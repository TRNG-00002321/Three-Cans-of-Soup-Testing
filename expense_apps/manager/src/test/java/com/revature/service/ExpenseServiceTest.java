package com.revature.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.revature.repository.ApprovalRepository;

public class ExpenseServiceTest {

    @Mock
    private ApprovalRepository approvalRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveExpense_SuccessfulApproval_ReturnsTrue() {
        when(approvalRepository.updateApprovalStatus(123, "approved", 456, "Looks good"))
                .thenReturn(true);

        boolean result = expenseService.approveExpense(123, 456, "Looks good");

        assertTrue(result);
        verify(approvalRepository).updateApprovalStatus(123, "approved", 456, "Looks good");
    }

    @Test
    void approveExpense_FailedApproval_ReturnsFalse() {
        when(approvalRepository.updateApprovalStatus(999, "approved", 456, "Test"))
                .thenReturn(false);

        boolean result = expenseService.approveExpense(999, 456, "Test");

        assertFalse(result);
        verify(approvalRepository).updateApprovalStatus(999, "approved", 456, "Test");
    }

    @Test
    void approveExpense_DatabaseError_ThrowsRuntimeException() {
        when(approvalRepository.updateApprovalStatus(999, "approved", 100, "Approved"))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> expenseService.approveExpense(999, 100, "Approved")
        );

        verify(approvalRepository).updateApprovalStatus(999, "approved", 100, "Approved");
    }

    @Test
    void denyExpense_SuccessfulDenial_ReturnsTrue() {
        when(approvalRepository.updateApprovalStatus(123, "denied", 456, "Insufficient docs"))
                .thenReturn(true);

        boolean result = expenseService.denyExpense(123, 456, "Insufficient docs");

        assertTrue(result);
        verify(approvalRepository).updateApprovalStatus(123, "denied", 456, "Insufficient docs");
    }

    @Test
    void denyExpense_FailedDenial_ReturnsFalse() {
        when(approvalRepository.updateApprovalStatus(999, "denied", 456, "Test"))
                .thenReturn(false);

        boolean result = expenseService.denyExpense(999, 456, "Test");

        assertFalse(result);
        verify(approvalRepository).updateApprovalStatus(999, "denied", 456, "Test");
    }

    @Test
    void denyExpense_DatabaseError_ThrowsRuntimeException() {
        when(approvalRepository.updateApprovalStatus(999, "denied", 100, "Denied"))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> expenseService.denyExpense(999, 100, "Denied")
        );

        verify(approvalRepository).updateApprovalStatus(999, "denied", 100, "Denied");
    }
}
