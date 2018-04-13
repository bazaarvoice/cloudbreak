package com.sequenceiq.cloudbreak.blueprint.sharedservice;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Stack;

@Component
public class SharedServiceConfigsProvider {

    public SharedServiceConfigs createSharedServiceConfigs(Blueprint blueprint, String ambariPassword, Stack dataLakeStack) {
        SharedServiceConfigs sharedServiceConfigs = new SharedServiceConfigs();
        if (dataLakeStack != null) {
            sharedServiceConfigs.setRangerAdminPassword(dataLakeStack.getCluster().getPassword());
            sharedServiceConfigs.setAttachedCluster(true);
            sharedServiceConfigs.setDatalakeCluster(false);
        } else if (isSharedServiceReqdyBlueprint(blueprint)) {
            sharedServiceConfigs.setRangerAdminPassword(ambariPassword);
            sharedServiceConfigs.setAttachedCluster(false);
            sharedServiceConfigs.setDatalakeCluster(true);
        } else {
            sharedServiceConfigs.setRangerAdminPassword(ambariPassword);
            sharedServiceConfigs.setAttachedCluster(false);
            sharedServiceConfigs.setDatalakeCluster(false);
        }
        return sharedServiceConfigs;
    }

    public SharedServiceConfigs createSharedServiceConfigs(Stack source, Stack dataLakeStack) {
        return createSharedServiceConfigs(source.getCluster().getBlueprint(), source.getCluster().getPassword(), dataLakeStack);
    }

    private boolean isSharedServiceReqdyBlueprint(Blueprint blueprint) {
        return blueprint.getTags() != null && blueprint.getTags().getMap().containsKey("shared_services_ready");
    }
}
