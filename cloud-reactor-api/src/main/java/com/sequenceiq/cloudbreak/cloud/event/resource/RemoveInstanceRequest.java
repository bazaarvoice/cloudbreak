package com.sequenceiq.cloudbreak.cloud.event.resource;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

public class RemoveInstanceRequest extends CloudStackRequest<RemoveInstanceResult> {

    private final List<CloudResource> cloudResources;

    private final List<CloudInstance> instances;

    private Boolean forceHealthyInstanceDeletion;

    public RemoveInstanceRequest(CloudContext cloudContext, CloudCredential cloudCredential, CloudStack cloudStack,
            List<CloudResource> cloudResources, List<CloudInstance> instances, Boolean forceHealthyInstanceDeletion) {
        super(cloudContext, cloudCredential, cloudStack);
        this.cloudResources = cloudResources;
        this.instances = instances;
        this.forceHealthyInstanceDeletion = forceHealthyInstanceDeletion;
    }

    public List<CloudResource> getCloudResources() {
        return cloudResources;
    }

    public List<CloudInstance> getInstances() {
        return instances;
    }

    public Boolean getForceHealthyInstanceDeletion() {
        return forceHealthyInstanceDeletion;
    }
}
