package com.sequenceiq.cloudbreak.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import com.google.common.collect.Lists;
import com.sequenceiq.cloudbreak.api.model.Status;
import com.sequenceiq.cloudbreak.core.flow2.Flow2Handler;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StateStatus;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class RetryOperationServiceTest {

    private static final Long STACK_ID = 1L;

    private static final String FLOW_ID = "flowId";

    @InjectMocks
    private RetryOperationService underTest;

    @Mock
    private Flow2Handler flow2Handler;

    @Mock
    private FlowLogRepository flowLogRepository;

    @Mock
    private Stack stackMock;

    @Mock
    private AuthorizationService authorizationService;

    @Test
    public void retryPending() {
        when(stackMock.getId()).thenReturn(STACK_ID);

        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                createFlowLog("START_STATE", StateStatus.PENDING),
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL)
                );
        when(flowLogRepository.findAllByStackIdOrderByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(0)).restartFlow(any(FlowLog.class));
    }

    private FlowLog createFlowLog(String currentState, StateStatus stateStatus) {
        return new FlowLog(STACK_ID, FLOW_ID, currentState, true, stateStatus);
    }

    @Test
    public void retrySuccessful() {
        when(stackMock.getId()).thenReturn(STACK_ID);
        when(stackMock.getStatus()).thenReturn(Status.AVAILABLE);

        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                createFlowLog("START_STATE", StateStatus.SUCCESSFUL),
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL)
        );
        when(flowLogRepository.findAllByStackIdOrderByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(0)).restartFlow(any(FlowLog.class));
    }

    @Test
    public void retry() {
        when(stackMock.getId()).thenReturn(STACK_ID);

        FlowLog failedState = createFlowLog("START_STATE", StateStatus.FAILED);
        List<FlowLog> pendingFlowLogs = Lists.newArrayList(
                failedState,
                createFlowLog("INIT_STATE", StateStatus.SUCCESSFUL)
        );
        when(flowLogRepository.findAllByStackIdOrderByCreatedDesc(STACK_ID)).thenReturn(pendingFlowLogs);
        underTest.retry(stackMock);

        verify(flow2Handler, times(1)).restartFlow(ArgumentMatchers.eq(failedState));
    }

    @Test(expected = AccessDeniedException.class)
    public void retryNoWritePermission() {
        doThrow(new AccessDeniedException("No write rights")).when(authorizationService).hasWritePermission(stackMock);

        underTest.retry(stackMock);
    }
}