package de.unibi.cebitec.bibigrid.openstack;

import de.unibi.cebitec.bibigrid.core.CommandLineValidator;
import de.unibi.cebitec.bibigrid.core.intents.*;
import de.unibi.cebitec.bibigrid.core.model.*;
import de.unibi.cebitec.bibigrid.core.model.exceptions.ClientConnectionFailedException;
import de.unibi.cebitec.bibigrid.core.model.exceptions.ConfigurationException;
import de.unibi.cebitec.bibigrid.core.util.DefaultPropertiesFile;
import org.apache.commons.cli.CommandLine;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;

import java.util.Map;
import java.util.HashMap;

/**
 * @author mfriedrichs(at)techfak.uni-bielefeld.de
 */
@SuppressWarnings("unused")
public class ProviderModuleOpenstack extends ProviderModule {
    @Override
    public String getName() {
        return "openstack";
    }

    @Override
    public CommandLineValidator getCommandLineValidator(final CommandLine commandLine,
                                                        final DefaultPropertiesFile defaultPropertiesFile,
                                                        final IntentMode intentMode)
            throws ConfigurationException {
        return new CommandLineValidatorOpenstack(commandLine, defaultPropertiesFile, intentMode, this);
    }

    @Override
    public Client getClient(Configuration config) throws ClientConnectionFailedException {
        return new ClientOpenstack((ConfigurationOpenstack) config);
    }

    @Override
    public ListIntent getListIntent(Client client, Configuration config) {
        return new ListIntentOpenstack(this, client, config);
    }

    @Override
    public TerminateIntent getTerminateIntent(Client client, Configuration config) {
        return new TerminateIntentOpenstack(this, client, config);
    }

    @Override
    public PrepareIntent getPrepareIntent(Client client, Configuration config) {
        return new PrepareIntentOpenstack(this, client, config);
    }

    @Override
    public CreateCluster getCreateIntent(Client client, Configuration config) {
        return new CreateClusterOpenstack(this, client, config);
    }

    @Override
    public CreateClusterEnvironment getClusterEnvironment(Client client, CreateCluster cluster) throws ConfigurationException {
        return new CreateClusterEnvironmentOpenstack(client, (CreateClusterOpenstack) cluster);
    }

    @Override
    public String getBlockDeviceBase() {
        return "/dev/vd";
    }

    @Override
    protected Map<String, InstanceType> getInstanceTypeMap(Client client, Configuration config) {
        OSClient os = ((ClientOpenstack) client).getInternal();
        Map<String, InstanceType> instanceTypes = new HashMap<>();
        for (Flavor f : os.compute().flavors().list()) {
            instanceTypes.put(f.getName(), new InstanceTypeOpenstack(f));
        }
        return instanceTypes;
    }
}
