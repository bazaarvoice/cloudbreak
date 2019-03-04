package com.sequenceiq.cloudbreak.orchestrator.salt.client.target;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.stream.Collectors;

public class BVCompound extends Compound {

    private final Collection<String> targets;

    public BVCompound(Collection<String> targets) {
        super(ImmutableList.of());

        this.targets = targets;
    }

    @Override
    public String getTarget() {
        return targets.stream()
            .map(this::ipToHostTargetGlob)
            .collect(Collectors.joining(" or "));
    }

    private String ipToHostTargetGlob(String ip) {
        return "ip-" + ip.replace('.', '-') + "*";
    }
}
