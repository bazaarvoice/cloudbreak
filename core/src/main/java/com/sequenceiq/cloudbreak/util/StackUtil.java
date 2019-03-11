package com.sequenceiq.cloudbreak.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.service.CloudbreakException;
import com.sequenceiq.cloudbreak.common.model.OrchestratorType;
import com.sequenceiq.cloudbreak.core.bootstrap.service.OrchestratorTypeResolver;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.view.StackView;
import com.sequenceiq.cloudbreak.orchestrator.model.Node;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;

@Service
public class StackUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackUtil.class);

    /**
     * Instances created recently will be at least a few hours ago
     */
    private static final int RECENTLY_CREATED_HOUR_S = 2 * 3600;

    private static final Date SINCE_1970 = new Date(0L);

    @Inject
    private OrchestratorTypeResolver orchestratorTypeResolver;

    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;

    public Set<Node> collectNodes(Stack stack) {
        return collectNodes(stack, false);
    }

    public Set<Node> collectProvisioningNodes(Stack stack) {
        return collectNodes(stack, true);
    }

    public Set<Node> collectRecentlyCreatedNodes(Stack stack) {
        return collectNodes(stack, false, Date.from(Instant.now().minusSeconds(RECENTLY_CREATED_HOUR_S)));
    }

    private Set<Node> collectNodes(Stack stack, boolean provisioning) {
        return collectNodes(stack, provisioning, SINCE_1970);
    }

    private Set<Node> collectNodes(Stack stack, boolean provisioning, Date createdSince) {
        Set<Node> agents = new HashSet<>();
        for (InstanceGroup instanceGroup : stack.getInstanceGroups()) {
            if (instanceGroup.getNodeCount() != 0) {
                Set<InstanceMetaData> instances = provisioning
                    ? instanceGroup.getProvisioningInstanceMetaDataSet()
                    : instanceGroup.getNotDeletedInstanceMetaDataSet();

                instances = instances.stream()
                    .filter(im -> im.getStartDate() >= createdSince.getTime())
                    .collect(Collectors.toSet());

                for (InstanceMetaData im : instances) {
                    if (im.getDiscoveryFQDN() != null) {
                        agents.add(new Node(im.getPrivateIp(), im.getPublicIp(), im.getDiscoveryFQDN(), im.getInstanceGroupName()));
                    }
                }
            }
        }
        return agents;
    }

    public String extractAmbariIp(StackView stackView) {
        return extractAmbariIp(stackView.getId(), stackView.getOrchestrator().getType(),
                stackView.getClusterView() != null ? stackView.getClusterView().getAmbariIp() : null);
    }

    public String extractAmbariIp(Stack stack) {
        return extractAmbariIp(stack.getId(), stack.getOrchestrator().getType(), stack.getCluster() != null ? stack.getCluster().getAmbariIp() : null);
    }

    private String extractAmbariIp(long stackId, String orchestratorName, String ambariIp) {
        String result = null;
        try {
            OrchestratorType orchestratorType = orchestratorTypeResolver.resolveType(orchestratorName);
            if (orchestratorType != null && orchestratorType.containerOrchestrator()) {
                result = ambariIp;
            } else {
                InstanceMetaData gatewayInstance = instanceMetaDataRepository.getPrimaryGatewayInstanceMetadata(stackId);
                if (gatewayInstance != null) {
                    result = gatewayInstance.getPublicIpWrapper();
                }
            }
        } catch (CloudbreakException ex) {
            LOGGER.error("Could not resolve orchestrator type: ", ex);
        }
        return result;
    }

    public long getUptimeForCluster(Cluster cluster, boolean addUpsinceToUptime) {
        Duration uptime = Duration.ZERO;
        if (StringUtils.isNotBlank(cluster.getUptime())) {
            uptime = Duration.parse(cluster.getUptime());
        }
        if (cluster.getUpSince() != null && addUpsinceToUptime) {
            long now = new Date().getTime();
            uptime = uptime.plusMillis(now - cluster.getUpSince());
        }
        return uptime.toMillis();
    }
}
