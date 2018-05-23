package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.event.InstancePayload;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

/**
 * @deprecated Downscale flow is used instead of Instance termination flow
 */
@Deprecated
public class InstanceTerminationTriggerEvent extends StackEvent implements InstancePayload {
    private final Set<String> instanceIds;

    private Boolean forceHealthyInstanceDeletion;

    public InstanceTerminationTriggerEvent(String selector, Long stackId, Set<String> instanceIds, Boolean forceHealthyInstanceDeletion) {
        super(selector, stackId);
        this.instanceIds = instanceIds;
        this.forceHealthyInstanceDeletion = forceHealthyInstanceDeletion;
    }

    @Override
    public Set<String> getInstanceIds() {
        return instanceIds;
    }

    @Override
    public Boolean getForceHealthyInstanceDeletion() {
        return forceHealthyInstanceDeletion;
    }
}
