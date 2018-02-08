package de.unibi.cebitec.bibigrid.core.util;

import de.unibi.cebitec.bibigrid.core.model.Configuration;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static de.unibi.cebitec.bibigrid.core.util.VerboseOutputFilter.V;

/**
 * @author mfriedrichs(at)techfak.uni-bielefeld.de
 */
public final class DefaultPropertiesFile {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPropertiesFile.class);
    private static final String DEFAULT_DIRNAME = System.getProperty("user.home");
    private static final String DEFAULT_FILENAME = ".bibigrid.yml";
    private static final String PROPERTIES_FILEPATH_PARAMETER = "o";

    private Path propertiesFilePath;
    private boolean isAlternativeFilepath;
    private String propertiesMode;

    public DefaultPropertiesFile(CommandLine commandLine) {
        if (commandLine.hasOption(PROPERTIES_FILEPATH_PARAMETER)) {
            String path = commandLine.getOptionValue(PROPERTIES_FILEPATH_PARAMETER);
            Path newPath = FileSystems.getDefault().getPath(path);
            if (Files.isReadable(newPath)) {
                propertiesFilePath = newPath;
                isAlternativeFilepath = true;
                LOG.info("Alternative config file {} will be used.", propertiesFilePath.toString());
            } else {
                LOG.error("Alternative config ({}) file is not readable. Try to use default.", newPath.toString());
            }
        }
        if (propertiesFilePath == null) {
            propertiesFilePath = FileSystems.getDefault().getPath(DEFAULT_DIRNAME, DEFAULT_FILENAME);
        }
        if (Files.exists(propertiesFilePath)) {
            LOG.info(V, "Reading default options from properties file at '{}'.", propertiesFilePath);
            try {
                // In order to load the yaml file directly into the provider Configuration we have to peek the mode
                Map<String, String> yamlMap = new Yaml().load(new FileInputStream(propertiesFilePath.toFile()));
                propertiesMode = yamlMap.getOrDefault("mode", null);
            } catch (FileNotFoundException e) {
                LOG.error("Failed to load mode parameter from properties file.");
            }
        } else {
            LOG.info("No properties file for default options found ({}). Using command line parameters only.",
                    propertiesFilePath);
        }
    }

    public boolean isAlternativeFilepath() {
        return isAlternativeFilepath;
    }

    public Path getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getPropertiesMode() {
        return propertiesMode;
    }

    public Configuration loadConfiguration(Class<? extends Configuration> configurationClass) {
        if (Files.exists(propertiesFilePath)) {
            try {
                return new Yaml().loadAs(new FileInputStream(propertiesFilePath.toFile()), configurationClass);
            } catch (FileNotFoundException e) {
                LOG.error("Failed to load properties file. {}", e);
            }
        }
        try {
            return configurationClass.getConstructor().newInstance();
        } catch (Exception e) {
            LOG.error("Failed to instantiate empty configuration. {}", e);
        }
        return null;
    }
}
