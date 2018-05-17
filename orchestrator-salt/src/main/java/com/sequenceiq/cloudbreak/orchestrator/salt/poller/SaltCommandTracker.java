package com.sequenceiq.cloudbreak.orchestrator.salt.poller;

import com.sequenceiq.cloudbreak.orchestrator.OrchestratorBootstrap;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.salt.client.SaltConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaltCommandTracker implements OrchestratorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaltCommandTracker.class);

    private final SaltConnector saltConnector;

    private final SaltJobRunner saltJobRunner;

    public SaltCommandTracker(SaltConnector saltConnector, SaltJobRunner saltJobRunner) {
        this.saltConnector = saltConnector;
        this.saltJobRunner = saltJobRunner;
    }

    @Override
    public Boolean call() throws Exception {
        LOGGER.info("SaltCommandTracker call Before Submit");
        saltJobRunner.submit(saltConnector);
        LOGGER.info("SaltCommandTracker call After Submit");
        if (!saltJobRunner.getTarget().isEmpty()) {
            throw new CloudbreakOrchestratorFailedException("There are missing nodes from job result: " + saltJobRunner.getTarget());
        }
        return true;
    }

    @Override
    public String toString() {
        return "SaltCommandTracker{"
                + "saltJobRunner=" + saltJobRunner
                + '}';
    }
}
