package de.unibi.cebitec.bibigrid.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Image;

import de.unibi.cebitec.bibigrid.intents.ValidateIntent;
import de.unibi.cebitec.bibigrid.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.unibi.cebitec.bibigrid.util.ImportantInfoOutputFilter.I;
import static de.unibi.cebitec.bibigrid.util.VerboseOutputFilter.V;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Johannes Steiner - jsteiner(at)cebitec.uni-bielefeld.de
 */
public class ValidateIntentAWS extends IntentAWS implements ValidateIntent {
    public static final Logger LOG = LoggerFactory.getLogger(ValidateIntentAWS.class);
    private final Configuration conf;
    private AmazonEC2Client ec2;

    ValidateIntentAWS(final Configuration conf) {
        this.conf = conf;
    }

    @Override
    public boolean validate() {
        LOG.info("Validating config file...");
        // Access Key Check
        ec2 = getClient(conf);
        boolean success = true;
        try {
            DryRunSupportedRequest<CreateTagsRequest> tryKeys = () -> new CreateTagsRequest().getDryRunRequest();
            DryRunResult dryRunResult = ec2.dryRun(tryKeys);
            if (dryRunResult.isSuccessful()) {
                LOG.info(I, "Access Key Test successful.");
            } else {
                LOG.error("AccessKey test not successful. Please check your configuration.");
                return false;
            }
        } catch (AmazonClientException e) {
            LOG.error("The access or secret key does not seem to valid.");
            return false;
        }
        sleep(1);
        if (checkImages()) {
            LOG.info(I, "Image check has been successful.");
        } else {
            success = false;
            LOG.error("There were one or more errors during the last step.");
        }
        sleep(1);
        if (checkSnapshots()) {
            LOG.info(I, "Snapshot check has been successful.");
        } else {
            success = false;
            LOG.error("One or more snapshots could not be found.");
        }
        sleep(1);
        if (success) {
            LOG.info(I, "You can now start your cluster.");
        } else {
            LOG.error("There were one or more errors. Please adjust your configuration.");
        }
        return true;
    }

    private boolean checkSnapshots() {
        LOG.info("Checking snapshots");
        boolean allCheck = true;
        conf.getMasterMounts().keySet();
        List<String> snapShotList = new ArrayList<>(conf.getMasterMounts().keySet());
        snapShotList.addAll(conf.getSlaveMounts().keySet());
        // snapshot ids have to be checked individually to find out which one is missing or malformed.
        for (String e : snapShotList) {
            try {
                if (e.contains(":")) {
                    e = e.substring(0, e.indexOf(":"));
                }
                DescribeSnapshotsRequest snapshotRequest = new DescribeSnapshotsRequest().withSnapshotIds(e);
                DescribeSnapshotsResult snapshotResult = ec2.describeSnapshots(snapshotRequest);
                if (snapshotResult.getSnapshots().get(0).getSnapshotId().equals(e)) {
                    LOG.info(V, "{} found.", e);
                }
            } catch (AmazonServiceException f) {
                LOG.error("Snapshot {} could not be found.", e);
                allCheck = false;
            }
        }
        return allCheck;
    }

    private boolean checkImages() {
        LOG.info("Checking Images...");
        boolean allCheck = true;
        // Checking for Images in Config File
        try {
            DescribeImagesRequest imageRequest = new DescribeImagesRequest().withImageIds(
                    Arrays.asList(conf.getMasterImage(), conf.getSlaveImage()));
            DescribeImagesResult imageResult = ec2.describeImages(imageRequest);
            boolean slave = false, master = false;
            boolean masterClusterType = conf.getMasterInstanceType().getSpec().isClusterInstance();
            boolean slaveClusterType = conf.getSlaveInstanceType().getSpec().isClusterInstance();
            // Checking if both are hvm or paravirtual types
            if (masterClusterType != slaveClusterType) {
                LOG.error("If cluster instances are used please create a homogeneous group.");
                allCheck = false;
            } else if (masterClusterType) {
                // If master instance is a cluster instance check if the types are the same
                if (conf.getMasterInstanceType() != conf.getSlaveInstanceType()) {
                    LOG.error("If cluster instances are used please create a homogeneous group.");
                    allCheck = false;
                }
            }
            for (Image image : imageResult.getImages()) {
                // Checking if Master Image is available.
                if (image.getImageId().equals(conf.getMasterImage())) {
                    master = true;
                    if (image.getVirtualizationType().equals("hvm")) {
                        // Image detected is of HVM Type
                        if (conf.getMasterInstanceType().getSpec().isHvm()) {
                            // Instance and Image is HVM type
                            LOG.info(I, "Master instance can use HVM images.");
                        } else if (conf.getMasterInstanceType().getSpec().isPvm()) {
                            // HVM Image but instance type is not correct
                            LOG.error("Master Instance type does not support hardware-assisted virtualization.");
                            allCheck = false;
                        }
                    } else {
                        if (conf.getMasterInstanceType().getSpec().isPvm()) {
                            // Instance and Image fits.
                            LOG.info(I, "Master instance can use paravirtual images.");
                        } else if (conf.getMasterInstanceType().getSpec().isHvm()) {
                            // Paravirtual Image but cluster instance type
                            LOG.error("Master Instance type does not support paravirtual images.");
                            allCheck = false;
                        }
                    }
                }
                // Checking if Slave Image is available.
                if (image.getImageId().equals(conf.getSlaveImage())) {
                    slave = true;
                    if (image.getVirtualizationType().equals("hvm")) {
                        // Image detected is of HVM Type
                        if (conf.getSlaveInstanceType().getSpec().isHvm()) {
                            // Instance and Image is HVM type
                            LOG.info(I, "Slave instance can use HVM images.");
                        } else if (conf.getSlaveInstanceType().getSpec().isPvm()) {
                            // HVM Image but instance type is not correct
                            LOG.error("Slave Instance type does not support hardware-assisted virtualization.");
                            allCheck = false;
                        }
                    } else {
                        if (conf.getSlaveInstanceType().getSpec().isPvm()) {
                            // Instance and Image fits.
                            LOG.info(I, "Slave instance can use paravirtual images.");
                        } else if (conf.getSlaveInstanceType().getSpec().isHvm()) {
                            // Paravirtual Image but cluster instance type
                            LOG.error("Slave Instance type does not support paravirtual images.");
                            allCheck = false;
                        }
                    }
                }
            }
            if (slave && master) {
                LOG.info(I, "Master and Slave AMIs have been found.");
            } else {
                LOG.error("Master and Slave AMIs could not be found.");
                allCheck = false;
            }
        } catch (AmazonServiceException e) {
            LOG.error("Master and Slave AMIs could not be found. Check if the ID is malformed (ami-XXXXXXXX).");
            allCheck = false;
        }
        return allCheck;
    }
}
