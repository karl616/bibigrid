package de.unibi.cebitec.bibigrid.aws;

import com.amazonaws.services.ec2.model.*;
import de.unibi.cebitec.bibigrid.core.intents.ListIntent;

import java.util.*;
import java.util.stream.Collectors;

import de.unibi.cebitec.bibigrid.core.model.*;
import de.unibi.cebitec.bibigrid.core.model.Instance;
import de.unibi.cebitec.bibigrid.core.model.exceptions.InstanceTypeNotFoundException;

/**
 * Implementation of the general ListIntent interface for an AWS based cluster.
 *
 * @author Jan Krueger - jkrueger(at)cebitec.uni-bielefeld.de
 */
public class ListIntentAWS extends ListIntent {
    ListIntentAWS(final ProviderModule providerModule, Client client, final Configuration config) {
        super(providerModule, client, config);
    }

    @Override
    protected List<Instance> getInstances() {
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        DescribeInstancesResult result = ((ClientAWS) client).getInternal().describeInstances(request);
        return result.getReservations().stream().flatMap(r -> r.getInstances().stream())
                .map(i -> new InstanceAWS(null, i)).collect(Collectors.toList());
    }

    @Override
    protected void checkInstance(Instance instance) {
        InstanceAWS instanceAWS = (InstanceAWS) instance;
        if (instanceAWS.getState().equals("pending") || instanceAWS.getState().equals("running") ||
                instanceAWS.getState().equals("stopping") || instanceAWS.getState().equals("stopped")) {
            super.checkInstance(instance);
        }
    }

    @Override
    protected void loadInstanceConfiguration(Instance instance) {
        com.amazonaws.services.ec2.model.Instance internalInstance = ((InstanceAWS) instance).getInternal();
        Configuration.InstanceConfiguration instanceConfiguration = new Configuration.InstanceConfiguration();
        instanceConfiguration.setType(internalInstance.getInstanceType());
        try {
            instanceConfiguration.setProviderType(providerModule.getInstanceType(client, config, internalInstance.getInstanceType()));
        } catch (InstanceTypeNotFoundException ignored) {
        }
        instanceConfiguration.setImage(internalInstance.getImageId());
        instance.setConfiguration(instanceConfiguration);
    }
}
