package com.sequenceiq.cloudbreak.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.Status;
import com.sequenceiq.cloudbreak.core.flow2.Flow2Handler;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StateStatus;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;

@Service
public class RetryOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryOperationService.class);

    @Inject
    private Flow2Handler flow2Handler;

    @Inject
    private FlowLogRepository flowLogRepository;

    @Inject
    private AuthorizationService authorizationService;

    public void retry(Stack stack) {
        authorizationService.hasWritePermission(stack);

        List<FlowLog> flowLogs = flowLogRepository.findAllByStackIdOrderByCreatedDesc(stack.getId());
        if (isFlowPending(flowLogs)) {
            LOGGER.debug("Retry cannot be performed, because there is already an active flow. stackId: {}", stack.getId());
            return;
        }

        if (Status.AVAILABLE.equals(stack.getStatus())) {
            LOGGER.info("Retry cannot be performed, provision finished successfully. stackId: {}", stack.getId());
            return;
        }

        Optional<FlowLog> failedFlowLog = getMostRecentFailedLog(flowLogs);
        failedFlowLog.ifPresent(flow2Handler::restartFlow);
    }

    private Optional<FlowLog> getMostRecentFailedLog(List<FlowLog> flowLogs) {
        return flowLogs.stream()
                    .filter(log -> StateStatus.FAILED.equals(log.getStateStatus()))
                    .findFirst();
    }

    private boolean isFlowPending(List<FlowLog> flowLogs) {
        return flowLogs.stream()
                .anyMatch(fl -> StateStatus.PENDING.equals(fl.getStateStatus()));
    }
}
