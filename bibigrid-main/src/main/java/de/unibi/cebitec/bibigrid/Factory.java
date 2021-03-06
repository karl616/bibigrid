package de.unibi.cebitec.bibigrid;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Factory scanning the classpath for mapping implementations.
 *
 * @author mfriedrichs(at)techfak.uni-bielefeld.de
 */
public final class Factory {
    private static final List<String> IGNORED_JARS = Arrays.asList("rt.jar", "idea_rt.jar", "aws-java-sdk-ec2",
            "proto-", "google-cloud-", "google-api-", "openstack4j-core", "selenium-", "google-api-client", "jackson-",
            "guava", "jetty", "netty-", "junit-");
    private static Factory instance;
    private Map<String, List<Class<?>>> interfaceClassMap;
    private Map<String, List<Class<?>>> baseClassMap;

    private Factory() {
        interfaceClassMap = new HashMap<>();
        baseClassMap = new HashMap<>();
        loadAllClasses();
    }

    public static Factory getInstance() {
        return instance != null ? instance : (instance = new Factory());
    }

    /**
     * Load all classes in the classpath and search for usable implementations.
     */
    private void loadAllClasses() {
        Set<String> allClassPaths = new HashSet<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        extractUrls().forEach((URL url) -> {
            try {
                File file = new File(url.toURI());
                if (file.isDirectory()) {
                    iterateFileSystem(file, allClassPaths, url.toString());
                } else if (file.isFile() &&
                        file.getName().toLowerCase(Locale.US).endsWith(".jar") &&
                        IGNORED_JARS.stream().noneMatch(x -> file.getName().contains(x))) {
                    iterateJarFile(file, allClassPaths);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        });
        for (String classPath : allClassPaths) {
            loadClass(classLoader, classPath);
        }
    }

    private Stream<URL> extractUrls() {
        return Stream.of(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator)).map(this::toURL);
    }

    private URL toURL(String entry) {
        try {
            return new File(entry).toURI().toURL();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private void iterateFileSystem(File directory, Set<String> allClassPaths, String rootPath) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    iterateFileSystem(file, allClassPaths, rootPath);
                } else if (file.isFile()) {
                    collectUrl(allClassPaths, file.toURI().toURL().toString(), rootPath);
                }
            }
        }
    }

    private void iterateJarFile(File file, Set<String> allClassPaths) throws IOException {
        JarFile jarFile = new JarFile(file);
        for (Enumeration<JarEntry> je = jarFile.entries(); je.hasMoreElements(); ) {
            JarEntry j = je.nextElement();
            if (!j.isDirectory()) {
                collectUrl(allClassPaths, j.getName(), null);
            }
        }
    }

    private void collectUrl(Set<String> allClassPaths, String url, String rootPath) {
        if (url.endsWith(".class") && url.contains("de/unibi/cebitec/bibigrid")) {
            if (rootPath != null) {
                url = url.replace(rootPath, "");
            }
            allClassPaths.add(url.replace("/", ".").replace(".class", ""));
        }
    }

    private void loadClass(ClassLoader classLoader, String classPath) {
        Class<?> clazz = null;
        try {
            clazz = classLoader.loadClass(classPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            return;
        }
        for (Class<?> classInterface : clazz.getInterfaces()) {
            String interfaceName = classInterface.getName();
            if (!interfaceClassMap.containsKey(interfaceName)) {
                interfaceClassMap.put(interfaceName, new ArrayList<>());
            }
            interfaceClassMap.get(interfaceName).add(clazz);
        }
        if (clazz.getSuperclass() != null) {
            String superclassName = clazz.getSuperclass().getName();
            if (!baseClassMap.containsKey(superclassName)) {
                baseClassMap.put(superclassName, new ArrayList<>());
            }
            baseClassMap.get(superclassName).add(clazz);
        }
    }

    public <T> List<Class<T>> getImplementations(Class<T> type) {
        String typeName = type.getName();
        if (interfaceClassMap.containsKey(typeName)) {
            List<Class<T>> result = new ArrayList<>();
            for (Class<?> clazz : interfaceClassMap.get(typeName)) {
                //noinspection unchecked
                result.add((Class<T>) clazz);
            }
            return result;
        }
        if (baseClassMap.containsKey(typeName)) {
            List<Class<T>> result = new ArrayList<>();
            for (Class<?> clazz : baseClassMap.get(typeName)) {
                //noinspection unchecked
                result.add((Class<T>) clazz);
            }
            return result;
        }
        return Collections.emptyList();
    }
}
