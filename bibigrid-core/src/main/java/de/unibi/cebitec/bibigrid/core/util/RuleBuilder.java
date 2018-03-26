package de.unibi.cebitec.bibigrid.core.util;

import de.unibi.techfak.bibiserv.cms.Tparam;
import de.unibi.techfak.bibiserv.cms.TparamGroup;
import de.unibi.techfak.bibiserv.cms.Tprimitive;

import java.util.List;

public class RuleBuilder {
    private final TparamGroup rules = new TparamGroup();

    public RuleBuilder() {
        List<Object> group = rules.getParamrefOrParamGroupref();
        // Global program rules
        addBasicRule(group, RuleNames.VERBOSE, null, "more console output");
        addBasicRule(group, RuleNames.DEBUG_REQUESTS, null, "Enable HTTP request and response logging");

        addBasicRule(group, RuleNames.HELP_LIST_INSTANCE_TYPES, null, "Help: list instance types");

        // Master instance rules
        addStringRule(group, RuleNames.MASTER_INSTANCE_TYPE,
                "Master instance type to be used. Execute \"-h -lit\" for a complete list");
        addIntRule(group, RuleNames.MAX_MASTER_EPHEMERALS,
                "Limits the maximum number of used ephemerals for master spool volume (raid 0)", 1);
        addStringRule(group, RuleNames.MASTER_IMAGE,
                "Machine image id for master, if not set images defined at https://bibiserv.cebitec.uni-bielefeld.de" +
                        "/resources/bibigrid/<framework>/<region>.ami.properties are used!");
        // TODO: Regex for ex. search for =?
        addStringRule(group, RuleNames.MASTER_MOUNTS,
                "Comma-separated volume/snapshot id=mountpoint list (e.g. snap-12234abcd=/mnt/mydir1,snap-5667889ab=/mnt/mydir2) " +
                        "mounted to master. (Optional: Partition selection with ':', e.g. snap-12234abcd:1=/mnt/mydir1)");
        addBooleanRule(group, RuleNames.USE_MASTER_AS_COMPUTE,
                "[yes, no] if master should to be used as a compute instance");
        addBooleanRule(group, RuleNames.USE_MASTER_WITH_PUBLIC_IP,
                "[yes, no] if master should be used with a public ip address");

        // Slave instance rules
        addStringRule(group, RuleNames.SLAVE_INSTANCE_TYPE,
                "Slave instance type to be used. Execute \"-h -lit\" for a complete list");
        addIntRule(group, RuleNames.MAX_SLAVE_EPHEMERALS,
                "Limits the maximum number of used ephemerals for slave spool volume (raid 0)", 1);
        // TODO: "min: 0" but min.setValue(1) ?
        addIntRule(group, RuleNames.SLAVE_INSTANCE_COUNT, "min: 0", 1);
        addStringRule(group, RuleNames.SLAVE_IMAGE, "Machine image id for slaves, same behaviour like master-image");

        // Other rules
        addBooleanRule(group, RuleNames.USE_SPOT_INSTANCE_REQUEST, "[yes, no] if spot instances should be used");

        addStringRule(group, RuleNames.KEYPAIR, "Name of the keypair stored in the cloud provider console");
        addStringRule(group, RuleNames.SSH_PUBLIC_KEY_FILE, "Absolute path to public ssh key file");
        addStringRule(group, RuleNames.SSH_PRIVATE_KEY_FILE, "Absolute path to private ssh key file");
        addStringRule(group, RuleNames.REGION, "Region in which the cluster is created");
        addStringRule(group, RuleNames.AVAILABILITY_ZONE,
                "Specific zone in the provided region (e.g. AWS: eu-west-1a, Google: europe-west1-b)");

        Tparam port = addIntRule(group, RuleNames.PORTS, "Comma-separated list of additional ports (tcp & udp) to be " +
                "opened for all nodes (e.g. 80,443,8080). (Ignored if 'security-group' is set for Openstack!)");
        // Regex für Ports. TODO: Range Test ex. 0-255 useful?
        port.setRegexp("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(?:/(\\d{1,2}))*");

        addStringRule(group, RuleNames.SECURITY_GROUP, "Security group id used by current setup");
        addStringRule(group, RuleNames.NFS_SHARES,
                "Comma-separated list of paths on master to be shared via NFS (e.g. 192.168.10.44=/export/data,192.168.10.44=/export/bla)");
        addStringRule(group, RuleNames.EXT_NFS_SHARES, "Comma-separated nfsserver=path list");
        addStringRule(group, RuleNames.CONFIG, "Path to alternative config file");
        addStringRule(group, RuleNames.GRID_PROPERTIES_FILE,
                "Store essential grid properties like master & slave dns values and grid id in a Java property file");
        addStringRule(group, RuleNames.ROUTER, "Name of router used (Openstack)");
        addStringRule(group, RuleNames.NETWORK, "Name of network used");
        addStringRule(group, RuleNames.SUBNET, "Name of subnet used");
        addBooleanRule(group, RuleNames.PUBLIC_SLAVE_IP, "Slave instances also get an public ip address");
        addStringRule(group, RuleNames.META_MODE,
                "Allows you to use a different cloud provider. Available providers are listed above");
        Tparam localFs = addStringRule(group, RuleNames.LOCAL_FS,
                "[ext2, ext3, ext4, xfs] file system used for internal (ephemeral) diskspace. Default is 'xfs'");
        localFs.setRegexp("^((ext)[2-4]|(xfs))$");
        addStringRule(group, RuleNames.USER, "User name (for VM tagging)");
        addStringRule(group, RuleNames.SSH_USER, "SSH user name for master instance configuration");
        // AWS rules
        addStringRule(group, RuleNames.AWS_CREDENTIALS_FILE, "Containing access-key-id & secret-key");
        addBasicRule(group, RuleNames.BID_PRICE, Tprimitive.FLOAT, "bid price for spot instances");
        addIntRule(group, RuleNames.BID_PRICE_MASTER,
                "Bid price for the master spot instance, if not set general 'bidprice' is used.", 1);
        // Openstack rules
        addStringRule(group, RuleNames.OPENSTACK_USERNAME, "The given Openstack Username");
        addStringRule(group, RuleNames.OPENSTACK_TENANT_NAME, "The given Openstack Tenantname");
        addStringRule(group, RuleNames.OPENSTACK_PASSWORD, "The given Openstack User-Password");
        addStringRule(group, RuleNames.OPENSTACK_ENDPOINT, "The given Openstack Endpoint (e.g. https://xxx.xxx.xxx.xxx:5000/v3/)");
        addStringRule(group, RuleNames.OPENSTACK_DOMAIN, "The given Openstack Domain");
        addStringRule(group, RuleNames.OPENSTACK_TENANT_DOMAIN, "The given Openstack Tenant Domain");
        // Google compute engine rules
        addStringRule(group, RuleNames.GOOGLE_PROJECT_ID, "Google compute engine project id");
        addStringRule(group, RuleNames.GOOGLE_IMAGE_PROJECT_ID, "Google compute engine image project id");
        addStringRule(group, RuleNames.GOOGLE_CREDENTIALS_FILE, "Path to google compute engine service account credentials file");
        // Microsoft Azure rules
        addStringRule(group, RuleNames.AZURE_CREDENTIALS_FILE, "Path to microsoft azure credentials file");
        // Software rules
        addBooleanRule(group, RuleNames.MESOS, "[yes, no] if Mesos framework should be configured/started. Default is no");
        addBooleanRule(group, RuleNames.OPEN_GRID_ENGINE, "[yes, no] if OpenGridEngine should be configured/started. Default is yes");
        addBooleanRule(group, RuleNames.NFS, "[yes, no] if NFS should be configured/started. Default is yes");
        addBooleanRule(group, RuleNames.HDFS, "[yes, no] if HDFS should be configured/started. Default is no");
        addBooleanRule(group, RuleNames.CASSANDRA, "[yes, no] if Cassandra database should be configured/started. Default is no");
        addBooleanRule(group, RuleNames.SPARK, "[yes, no] if Spark cluster support should be configured/started. Default is no");
        addBooleanRule(group, RuleNames.CLOUD9, "[yes, no] if Cloud9 IDE should be configured/started. Default is no");
    }

    private Tparam addBasicRule(List<Object> group, RuleNames flag, Tprimitive type, String shortDescription) {
        Tparam tp = new Tparam();
        tp.setId(flag.getShortParam()); // shortFlag for short commands
        tp.setOption(flag.getLongParam()); // longFlag resp. option represent the long command
        Tparam.ShortDescription desc = new Tparam.ShortDescription();
        desc.setValue(shortDescription);
        tp.getShortDescription().add(desc);
        tp.setType(type);
        group.add(tp);
        return tp;
    }

    private Tparam addStringRule(List<Object> group, RuleNames flag, String shortDescription) {
        Tparam tp = addBasicRule(group, flag, Tprimitive.STRING, shortDescription);
        // if a string is needed it must not be empty
        tp.setMinLength(1);
        return tp;
    }

    private void addBooleanRule(List<Object> group, RuleNames flag, String shortDescription) {
        addBasicRule(group, flag, Tprimitive.BOOLEAN, shortDescription);
    }

    private Tparam addIntRule(List<Object> group, RuleNames flag, String shortDescription) {
        return addBasicRule(group, flag, Tprimitive.INT, shortDescription);
    }

    private void addIntRule(List<Object> group, RuleNames flag, String shortDescription, int min) {
        Tparam tp = addBasicRule(group, flag, Tprimitive.INT, shortDescription);
        Tparam.Min minValue = new Tparam.Min();
        minValue.setValue(min);
        tp.setMin(minValue);
    }

    public TparamGroup getRules() {
        return rules;
    }

    public enum RuleNames {
        MASTER_INSTANCE_TYPE("m", "master-instance-type"),
        MAX_MASTER_EPHEMERALS("mme", "max-master-ephemerals"),
        MASTER_IMAGE("M", "master-image"),
        SLAVE_INSTANCE_TYPE("s", "slave-instance-type"),
        MAX_SLAVE_EPHEMERALS("mse", "max-slave-ephemerals"),
        SLAVE_INSTANCE_COUNT("n", "slave-instance-count"),
        SLAVE_IMAGE("S", "slave-image"),
        USE_SPOT_INSTANCE_REQUEST("usir", "use-spot-instance-request"),
        KEYPAIR("k", "keypair"),
        SSH_PUBLIC_KEY_FILE("spu", "ssh-public-key-file"),
        SSH_PRIVATE_KEY_FILE("spr", "ssh-private-key-file"),
        REGION("e", "region", "OS_REGION_NAME"),
        AVAILABILITY_ZONE("z", "availability-zone"),
        PORTS("p", "ports"),
        SECURITY_GROUP("sg", "security-group"),
        MASTER_MOUNTS("d", "master-mounts"),
        NFS_SHARES("g", "nfs-shares"),
        EXT_NFS_SHARES("ge", "ext-nfs-shares"),
        VERBOSE("v", "verbose"),
        HELP_LIST_INSTANCE_TYPES("lit", "list-instance-types"),
        CONFIG("o", "config"),
        USE_MASTER_AS_COMPUTE("b", "use-master-as-compute"),
        USE_MASTER_WITH_PUBLIC_IP("pub", "use-master-with-public-ip"),
        HDFS("hdfs", "hdfs"),
        CASSANDRA("db", "cassandra"),
        SPARK("spark", "spark"),
        GRID_PROPERTIES_FILE("gpf", "grid-properties-file"),
        ROUTER("router", "router"),
        NETWORK("network", "network"),
        SUBNET("subnet", "subnet"),
        PUBLIC_SLAVE_IP("psi", "public-slave-ip"),
        MESOS("me", "mesos"),
        META_MODE("mode", "meta-mode"),
        OPEN_GRID_ENGINE("oge", "oge"),
        NFS("nfs", "nfs"),
        LOCAL_FS("lfs", "local-fs"),
        USER("u", "user"),
        SSH_USER("su", "ssh-user"),
        DEBUG_REQUESTS("dr", "debug-requests"),
        CLOUD9("c9", "cloud9"),
        // Amazon AWS
        AWS_CREDENTIALS_FILE("a", "aws-credentials-file"),
        BID_PRICE("bp", "bidprice"),
        BID_PRICE_MASTER("bpm", "bidprice-master"),
        // Microsoft Azure
        AZURE_CREDENTIALS_FILE("acf", "azure-credentials-file"),
        // Google Cloud
        GOOGLE_PROJECT_ID("gpid", "google-projectid"),
        GOOGLE_IMAGE_PROJECT_ID("gipid", "google-image-projectid"),
        GOOGLE_CREDENTIALS_FILE("gcf", "google-credentials-file"),
        // OpenStack
        OPENSTACK_USERNAME("osu", "openstack-username", "OS_USERNAME"),
        OPENSTACK_TENANT_NAME("ost", "openstack-tenantname", "OS_PROJECT_NAME"),
        OPENSTACK_PASSWORD("osp", "openstack-password", "OS_PASSWORD"),
        OPENSTACK_ENDPOINT("ose", "openstack-endpoint", "OS_AUTH_URL"),
        OPENSTACK_DOMAIN("osd", "openstack-domain", "OS_USER_DOMAIN_NAME"),
        OPENSTACK_TENANT_DOMAIN("ostd", "openstack-tenantdomain");

        private final String shortParam;
        private final String longParam;
        private final String envParam;

        RuleNames(String shortParam, String longParam) {
            this.shortParam = shortParam;
            this.longParam = longParam;
            envParam = null;
        }

        RuleNames(String shortParam, String longParam, String envParam) {
            this.shortParam = shortParam;
            this.longParam = longParam;
            this.envParam = envParam;
        }

        public boolean equals(String other) {
            return shortParam.equals(other);
        }

        public String getShortParam() {
            return shortParam;
        }

        public String getLongParam() {
            return longParam;
        }

        public String getEnvParam() {
            return envParam;
        }

        @Override
        public String toString() {
            return this.shortParam;
        }
    }
}