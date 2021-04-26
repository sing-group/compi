Custom runners
**************

What are Compi `runners`
========================

By default, compi runs task code by spawning local processes. With `runners`,
task' codes are passed to custom-made scripts which are in charge of running
them, for example, by submitting a job to a queue (e.g. Slurm, SGE) or using 
Docker images.

Runners are passed to the main ``compi run`` command using the ``-r`` 
parameter.

Creating a custom runner
========================

Like pipelines, runners are defined in XML. Individual runners are defined 
using the ``runner`` tag inside the ``runners`` tag. The ``task`` attribute 
is used to specify the list of tasks (comma-separated) that the corresponding 
runner must execute.

.. code-block:: xml

 <?xml version="1.0" encoding="UTF-8"?>
 <runners xmlns="http://sing-group.org/compi/runners-1.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <runner tasks="task-1, task-2">
         /bin/sh -c "${task_code}"
    </runner>
 </runners>

The runner code will have the following environment variables provided by compi:

- ``task_id``: contains the id of the task being executed.
- ``task_code``: contains the code (defined in the ``pipeline.xml``) of the task being executed.
- ``task_params``: contains the list of params associated to the task being executed.
- ``i``: in the case of ``foreach`` tasks, the iteration value.
- Like in regular Compi tasks, the task variables are also defined.

A simple example
----------------

Consider the following XML of the greetings pipeline:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0</version>
        <params>
            <param name="yourName" shortName="n" global="true" defaultValue="anonymous">Your name</param>
            <flag name="sayGoodBye" shortName="g">Do you want to say goodbye?</flag>
        </params>
        <tasks>
            <task id="greetings">
                echo "Hi ${yourName}"
            </task>          
            <task id="goodbye" 
                params="yourName sayGoodBye" if="[ -v sayGoodBye ]"
                after="greetings">
                echo "Goodbye ${yourName}"
            </task>
        </tasks>
    </pipeline>

And the following runners file where one runner is defined for the two 
pipeline tasks:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="greetings, goodbye">
            echo -e "[${task_id}] \n\tyourName: ${yourName} \n\tcode: ${task_code} \n\tparams: ${task_params}" >> /tmp/runner-output
            /bin/sh -c "${task_code}"
        </runner>
    </runners>

What this runner does is: printing task information (using the environment runner 
variables) into a file (``/tmp/runner-output``) and then running the task 
using the shell interpreter. This example can be executed with: 

.. code-block:: console

 compi run -p pipeline.xml -r runner.xml -o -- --sayGoodBye
 cat /tmp/runner-output

Examples of useful runners
==========================

Generic Docker runner
---------------------

Let's supose the following pipeline with one task to align a FASTA file using
Clustal Omega:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0</version>
        <params>
            <param name="workingDir" shortName="w" global="true">Working directory.</param>
            <param name="input" shortName="i" global="true">Input file.</param>
            <param name="output" shortName="o" global="true">Output file.</param>
            <param name="clustalomega" shortName="o" global="true" defaultValue="clustalo">Clustal Omega executable.</param>
        </params>
        <tasks>
            <task id="align">
                ${clustalomega} -i ${workingDir}/${input} -o ${workingDir}/${output}
            </task>          
        </tasks>
    </pipeline>

One may want to run this task using a Docker runner which runs the same task 
code inside a Docker container where the Clustal Omega executable is available.
The following runners file shows a runner to do this:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="align">
            envs=$(for param in $task_params; do echo -n "-e $param "; done)        
            docker run --rm $envs -v ${workingDir}:${workingDir} --entrypoint /bin/bash pegi3s/clustalomega -c "${task_code}"
        </runner>
    </runners>

The key points of this generic Docker runner are:

- The first line creates a variable with the list of parameters that should be passed to the Docker container as environment variables.
- The second line runs the docker image passing this list of environment variables and mounts the directory where the command has the input and output files.
- Since this particular image of Clustal Omega has an entrypoint defined, it must be overriden to run the desired task code.

Generic Slurm runner
--------------------

The following runners file shows a generic Slurm runner:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="task-1"> <!-- Replace tasks with the ids of your task to attach this runner to -->
            tmpfile=$(mktemp /tmp/compi-task-code.XXXXXXXX)
            echo "#!/bin/bash" >> ${tmpfile}
            echo ${task_code} >> ${tmpfile}
            chmod u+x ${tmpfile}
            srun -c 1 -p main --export ALL -o /tmp/task-1.log -e /tmp/task-1.err -J task_1 bash ${tmpfile}
        </runner>
    </runners>
    
Some parameters of the ``srun`` may need to be adjusted for each specific
cluster, but this is how a generic Slurm runner may look like. The 
``export`` parameter must be used to export all the environment variables to
the process that will be executed, and this is neccessary because the task
parameters are declared as environment variables.

Generic SSH runner
--------------------

The following runners file shows a generic SSH runner, that executes the task code in a given SSH host.
A confidence relation between the client machine (where Compi runs) and the remote host is assumed (See `here <https://www.thegeekstuff.com/2008/11/3-steps-to-perform-ssh-login-without-password-using-ssh-keygen-ssh-copy-id/>`_ how to create this)

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="task-1"> <!-- Replace tasks with the ids of your task to attach this runner to -->
            remote_host="192.168.1.108" #set here the remote machine
            remote_user="lipido"
            # copy the compi environment to a file 
            envfile=$(mktemp /tmp/compi-env.XXXXXX)
            for param in $task_params; do 
                export -p | sed -n -e "/^declare -x $param/,/^declare -x/ p" | sed \$d >> $envfile
                echo "export $param" >> $envfile
            done
            scp $envfile ${remote_user}@${remote_host}:${envfile}
            task_code_with_env="source $envfile; $task_code"
            ssh ${remote_user}@${remote_host} "$task_code_with_env"
        </runner>
    </runners>

Generic AWS runner
--------------------

Based on the previous SSH generic runner, here it is a more complex runner. This runner runs the task code over SSH in an Amazon Linux virtual machine.
In order to do that, this runner is in charge of creating the instance, if it is not available, and waiting for the SSH protocol being available.
After that, the task code is run in the instance via SSH. This runner uses `flock` utility to ensure that only one execution of the runner 
launches the Amazon instance, whereas the other ones only executes the SSH part.


.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="task-1">

            image_id="ami-014f0ecd3e71df934" # set here the AMI id
            remote_user="ubuntu" # SSH user of your amazon image (depends on the concrete image)
            type="m1.small" # set here the image type
            
            # Your Amazon key-pair (needed for SSH connection)
            key_name="lipido-aws-key" # you need to create a key pair in amazon
            private_key_file="~/.ssh/lipido-aws-key.pem"

            lock_file="/tmp/lock-for-launching-$image_id"
            touch ${lock_file} 
            
            # check if instance is available under a lock (only one instance of this runner will be inside this critical section)
            ( flock 99
                echo "Checking if amazon instance is available"
                OUT=$(aws ec2 describe-instances --filters "Name=tag-value,Values=compi-aws-${image_id}" Name=instance-state-name,Values=running --output text)
                if [ "$OUT" == "" ]; then
                    # Launch and tag the instance
                    echo "No. Launching a new amazon instance"
                    OUT=$(aws ec2 run-instances --image-id $image_id --instance-type $type --key-name ${key_name} --output text)
                    ID=$(echo "$OUT" | grep INSTANCES | cut -f 9)
                    STATE=$(echo "$OUT" | grep STATE | head -n 1 | cut -f 3)
                    aws ec2 create-tags --resources $ID --tags Key=Name,Value="compi-aws-${image_id}"
                    
                    # Wait for running state
                    echo "Wait for running state... "
                    echo "STATE is $STATE"
                    while [[ $STATE != running ]]; do
                        sleep 5
                        OUT=$(aws ec2 describe-instances --filters "Name=tag-value,Values=compi-aws-${image_id}" Name=instance-state-name,Values=running --output text)
                        STATE=$(echo "$OUT" | grep STATE | cut -f 3)   
                        echo "STATE is $STATE"
                    done    
                    echo "Running"

                    remote_host=$(echo "$OUT" | grep ASSOCIATION | head -n 1 | cut -f 3)
                    echo "remote host is $remote_host"

                    # Wait for SSH is ready
                    echo "Wait for SSH is ready"
                    READY=''
                    while [ ! $READY ]; do
                        sleep 10
                        set +e
                        OUT=$(ssh -o ConnectTimeout=1 -o StrictHostKeyChecking=no -o BatchMode=yes xxxx@${remote_host} 2>&1 | grep 'Permission denied' )
                        [[ $? = 0 ]] && READY='ready'
                        echo "READY is $READY"
                        set -e
                    done
                    echo "Ready"
                else
                    echo "Yes, it is available"
                fi
                
            ) 99<"$lock_file"

            # Here we assume that the instance is up and running

            # Obtain the instance details and host
            OUT=$(aws ec2 describe-instances --filters "Name=tag-value,Values=compi-aws-${image_id}" Name=instance-state-name,Values=running --output text)                
            remote_host=$(echo "$OUT" | grep ASSOCIATION | head -n 1 | cut -f 3)
            

            # copy the compi environment to a file 
            envfile=$(mktemp /tmp/compi-env.XXXXXX)
            for param in $task_params; do 
                export -p | sed -n -e "/^declare -x $param/,/^declare -x/ p" | sed \$d >> $envfile
                echo "export $param" >> $envfile
            done
            scp -o StrictHostKeyChecking=no -i ${private_key_file} $envfile ${remote_user}@${remote_host}:${envfile}
            task_code_with_env="source $envfile; $task_code"
            ssh -o StrictHostKeyChecking=no -i ${private_key_file} ${remote_user}@${remote_host} "$task_code_with_env"
        </runner>
    </runners>