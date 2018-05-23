package com.sequenceiq.cloudbreak.reactor.api.event.resource;

import java.util.Set;

public class CollectDownscaleCandidatesRequest extends AbstractClusterScaleRequest {
    private final Integer scalingAdjustment;

    private final Set<Long> privateIds;

    private Boolean forceHealthyInstanceDeletion;

    public CollectDownscaleCandidatesRequest(Long stackId, Boolean forceHealthyInstanceDeletion, String hostGroupName, Integer scalingAdjustment,
            Set<Long> privateIds) {
        super(stackId, hostGroupName);
        this.scalingAdjustment = scalingAdjustment;
        this.privateIds = privateIds;
        this.forceHealthyInstanceDeletion = forceHealthyInstanceDeletion;
    }

    public Integer getScalingAdjustment() {
        return scalingAdjustment;
    }

    public Set<Long> getPrivateIds() {
        return privateIds;
    }

    public Boolean getForceHealthyInstanceDeletion() {
        return forceHealthyInstanceDeletion;
    }
}
