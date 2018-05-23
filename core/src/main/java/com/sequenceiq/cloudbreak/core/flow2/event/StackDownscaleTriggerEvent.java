package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Set;

public class StackDownscaleTriggerEvent extends StackScaleTriggerEvent {

    private final Set<Long> privateIds;

    private final Boolean forceHealthyInstanceDeletion;

    public StackDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstance, String hostGroup, Integer adjustment) {
        super(selector, stackId, hostGroup, adjustment);
        privateIds = null;
        forceHealthyInstanceDeletion = forceHealtyInstance;
    }

    public StackDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstances, String instanceGroup, Set<Long> privateIds) {
        super(selector, stackId, instanceGroup, null);
        this.privateIds = privateIds;
        forceHealthyInstanceDeletion = forceHealtyInstances;
    }

    public Set<Long> getPrivateIds() {
        return privateIds;
    }

    public Boolean getForceHealthyInstanceDeletion() {
        return forceHealthyInstanceDeletion;
    }
}
