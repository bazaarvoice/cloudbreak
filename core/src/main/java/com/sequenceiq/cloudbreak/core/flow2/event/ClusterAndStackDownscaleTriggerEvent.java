package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Set;

import com.sequenceiq.cloudbreak.common.type.ScalingType;

import reactor.rx.Promise;

public class ClusterAndStackDownscaleTriggerEvent extends ClusterDownscaleTriggerEvent {
    private final ScalingType scalingType;

    public ClusterAndStackDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstance, String hostGroup,
            Integer adjustment, ScalingType scalingType) {
        super(selector, stackId, forceHealtyInstance, hostGroup, adjustment);
        this.scalingType = scalingType;
    }

    public ClusterAndStackDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstances, String hostGroup,
            Set<Long> privateIds, ScalingType scalingType, Promise<Boolean> accepted) {
        super(selector, stackId, forceHealtyInstances, hostGroup, privateIds, accepted);
        this.scalingType = scalingType;
    }

    public ScalingType getScalingType() {
        return scalingType;
    }
}
