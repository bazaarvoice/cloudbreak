package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Set;

import reactor.rx.Promise;

public class ClusterDownscaleTriggerEvent extends ClusterScaleTriggerEvent {
    private final Set<Long> privateIds;

    private final Boolean forceHealthyInstanceDeletion;

    public ClusterDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstance, String hostGroup, Integer adjustment) {
        super(selector, stackId, hostGroup, adjustment);
        privateIds = null;
        forceHealthyInstanceDeletion = forceHealtyInstance;
    }

    public ClusterDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstance, String hostGroup, Integer adjustment,
            Promise<Boolean> accepted) {
        super(selector, stackId, hostGroup, adjustment, accepted);
        privateIds = null;
        forceHealthyInstanceDeletion = forceHealtyInstance;
    }

    public ClusterDownscaleTriggerEvent(String selector, Long stackId, String hostGroup, Set<Long> privateIds) {
        super(selector, stackId, hostGroup, null);
        this.privateIds = privateIds;
        forceHealthyInstanceDeletion = false;
    }

    public ClusterDownscaleTriggerEvent(String selector, Long stackId, Boolean forceHealtyInstances, String hostGroup, Set<Long> privateIds,
            Promise<Boolean> accepted) {
        super(selector, stackId, hostGroup, null, accepted);
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
