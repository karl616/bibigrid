# Getting Started
Starting a cluster requires a valid configuration file and credentials. Following are the necessary steps with detailed information for each cloud provider.

## Setting up credentials
For communication with the cloud provider API, credentials have to be setup.
Additionally during cluster creation the master instance will handle software updates and installations for all cluster instances using ansible.
In order to upload and execute commands a valid ssh-keypair needs to be setup, too.

When using the ssh public key parameter in config or command line, the setup of ssh keys in the credentials setup can be skipped!
- [OpenStack credentials setup](../bibigrid-openstack/docs/Credentials_Setup.md)
- [Google compute credentials setup](../bibigrid-googlecloud/docs/Credentials_Setup.md)
- [Amazon AWS credentials setup](../bibigrid-aws/docs/Credentials_Setup.md)
- [Microsoft Azure credentials setup](../bibigrid-azure/docs/Credentials_Setup.md)

## Writing the configuration file
The configuration file specifies the composition of the requested cluster. Many parameters are shared across all cloud providers, however some parameters are provider specific.
You can either provide the necessary parameters via the command line, by using a configuration file in yaml format or in some cases by using environment variables.

A complete list of **command line parameters** can be found [here](COMMAND_LINE.md).

A complete schema for a **configuration file** can be found [here](CONFIGURATION_SCHEMA.md).

Provider specific examples representing the minimal required parameters:
* [Examples OpenStack](examples/EXAMPLES_OPENSTACK.md)
* [Examples Google Compute](examples/EXAMPLES_GOOGLECLOUD.md)
* [Examples AWS](examples/EXAMPLES_AWS.md)
* [Examples Azure](examples/EXAMPLES_AZURE.md)

### Writing and using a configuration file
The configuration file is a plain text file in YAML format. A short example would be:

```
#use google cloud compute
mode: googlecloud
googleProjectId: XXXXX
googleImageProjectId: ubuntu-os-cloud
credentialsFile: ~/google-credentials.json

region: europe-west1

network: default
subnet: default

user: testuser
sshUser: testuser
sshPrivateKeyFile: ~/cloud.ppk

masterInstance:
  type: f1-micro
  image: ubuntu-1604-xenial-v20171212

slaveInstances:
  - type: f1-micro
    count: 2
    image: ubuntu-1604-xenial-v20171212

ports:
  - type: TCP
    number: 80
  - type: TCP
    number: 443
```

This file can now be used with the "-o" command line parameter and the path to the configuration file.

## Validating the cluster configuration
Before starting the cluster directly after writing the configuration file, several components can be validated beforehand.
This prevents the majority of possible errors or typos, resulting in incomplete cluster setups.

```
> bibigrid -ch -o ~/config.yml
```

## Starting the cluster
Once the configuration is validated, the creation of the cluster can be started. Depending on the parameters
this may take some time.

```
> bibigrid -c -o ~/config.yml
```

## Starting the cloud9 IDE
If you activated the cloud9 feature in configuration, the IDE can be started with a simple command.
The process will run until an input is provided or it's terminated. The IDE is available under [http://localhost:8181](http://localhost:8181)

```
> bibigrid -cloud9 [cluster-id] -o ~/config.yml
```

## Cluster maintenance
### List running clusters
Once a cluster is created, it can be listed with the following command. All clusters found
with the selected provider will be listed, including some detail information.

```
> bibigrid -l -o ~/config.yml
```

Example output:

```
     cluster-id |       user |         launch date |             key name |       public-ip |  # inst |    group-id |   subnet-id |  network-id
-----------------------------------------------------------------------------------------------------------------------------------------------
fkiseokf34ekfeo |   testuser |   20/02/18 09:25:10 |          cluster-key |    XXX.XX.XX.XX |       3 | a45b6a63-.. |           - |           -
```

### Terminate the cluster
When you're finished using the cluster, you can terminate it using the following command and the logged cluster-id when the cluster was created.

```
> bibigrid -t [cluster-id] -o ~/config.yml
```

If necessary multiple clusters can be terminated at once.

```
> bibigrid -t [id1]/[id2]/[id3] -o ~/config.yml
```
