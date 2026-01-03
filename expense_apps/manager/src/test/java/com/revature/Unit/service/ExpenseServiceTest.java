package com.revature.Unit.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.repository.Approval;
import com.revature.repository.ApprovalRepository;
import com.revature.repository.Expense;
import com.revature.repository.ExpenseRepository;
import com.revature.repository.ExpenseWithUser;
import com.revature.repository.User;
import com.revature.service.ExpenseService;

@Epic("Manager App")
@Feature("Expense Management")
@Tag("Unit")
@Tag("Sprint-2")
@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    // @InjectMocks
    // private ExpenseService expenseService;
    // // class that contains generateCsvReport()
    @Mock
    private ExpenseWithUser expenseWithUser;

    @Mock
    private Expense expense;

    @Mock
    private User user;

    @Mock
    private Approval approval;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private List<ExpenseWithUser> getListOfExpensesWithUser() {
        ExpenseWithUser[] expenses = {
                new ExpenseWithUser(new Expense(), new User(), new Approval()),
                new ExpenseWithUser(new Expense(), new User(), new Approval()),
                new ExpenseWithUser(new Expense(), new User(), new Approval())
        };
        return Arrays.asList(expenses);
    }

    @Test
    @Story("Review Expenses")
    @Description("Verify retrieval of pending expenses")
    @Severity(SeverityLevel.NORMAL)
    public void getPendingExpenses_ReturnsExpenseList() {
        // Arrange
        when(expenseRepository.findPendingExpensesWithUsers()).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getPendingExpenses();

        // Assert
        verify(expenseRepository, times(1)).findPendingExpensesWithUsers();
    }

    @Test
    @Story("Review Expenses")
    @Description("Verify error handling for pending expenses")
    @Severity(SeverityLevel.CRITICAL)
    public void getPendingExpenses_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findPendingExpensesWithUsers())
                .thenThrow(new RuntimeException("Error finding pending expenses: "));

        // Act
        Executable action = () -> expenseService.getPendingExpenses();

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding pending expenses"));
        verify(expenseRepository, times(1)).findPendingExpensesWithUsers();
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 99, 453 })
    @Story("View All Expenses")
    @Description("Verify retrieval of expenses by employee ID")
    @Severity(SeverityLevel.NORMAL)
    public void getExpensesByEmployee_ReturnsExpenseList(int employeeId) {

        // Arrange
        when(expenseRepository.findExpensesByUser(employeeId)).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getExpensesByEmployee(employeeId);

        // Assert
        verify(expenseRepository, times(1)).findExpensesByUser(employeeId);
    }

    @Test
    @Story("View All Expenses")
    @Description("Verify error handling for employee expenses")
    @Severity(SeverityLevel.CRITICAL)
    public void getExpenseByEmployee_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findExpensesByUser(anyInt()))
                .thenThrow(new RuntimeException("Error finding expenses for user: "));

        // Act
        Executable action = () -> expenseService.getExpensesByEmployee(anyInt());

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding expenses for user"));
        verify(expenseRepository, times(1)).findExpensesByUser(anyInt());

    }

    @Test
    @Story("View All Expenses")
    @Description("Verify retrieval of all expenses")
    @Severity(SeverityLevel.NORMAL)
    public void getAllExpenses_ReturnsExpenseList() {
        // Arrange
        when(expenseRepository.findAllExpensesWithUsers()).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getAllExpenses();

        // Assert
        verify(expenseRepository, times(1)).findAllExpensesWithUsers();
    }

    @Test
    @Story("View All Expenses")
    @Description("Verify error handling for all expenses")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllExpenses_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findAllExpensesWithUsers())
                .thenThrow(new RuntimeException("Error finding expenses for user: "));

        // Act
        Executable action = () -> expenseService.getAllExpenses();

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding expenses for user"));
        verify(expenseRepository, times(1)).findAllExpensesWithUsers();
    }

    @Test
    @Story("Generate Reports")
    @Description("Verify CSV report generation with all fields present")
    @Severity(SeverityLevel.NORMAL)
    void generateCsvReportTestAllFields() {
        // Arrange
        when(expense.getId()).thenReturn(1);
        when(user.getUsername()).thenReturn("john_doe");
        when(expense.getAmount()).thenReturn(123.45);
        when(expense.getDescription()).thenReturn("Taxi ride");
        when(expense.getDate()).thenReturn("2025-01-01");

        when(expenseWithUser.getExpense()).thenReturn(expense);
        when(expenseWithUser.getUser()).thenReturn(user);
        when(expenseWithUser.getApproval()).thenReturn(approval);

        when(approval.getStatus()).thenReturn("APPROVED");
        when(approval.getReviewer()).thenReturn(10);
        when(approval.getComment()).thenReturn("Looks good");
        when(approval.getReviewDate()).thenReturn("2025-01-02");

        List<ExpenseWithUser> expenses = Collections.singletonList(expenseWithUser);

        // Act
        String result = expenseService.generateCsvReport(expenses);

        // Assert
        String expected = "Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n"
                + "1,john_doe,123.45,Taxi ride,2025-01-01,APPROVED,10,Looks good,2025-01-02\n";

        assertEquals(expected, result);
    }

    @Test
    @Story("Generate Reports")
    @Description("Verify CSV report generation for pending expenses")
    @Severity(SeverityLevel.NORMAL)
    void generateCsvReportTestPending() {
        // Arrange
        when(expense.getId()).thenReturn(2);
        when(user.getUsername()).thenReturn("alice");
        when(expense.getAmount()).thenReturn(50.00);
        when(expense.getDescription()).thenReturn("Lunch");
        when(expense.getDate()).thenReturn("2025-01-03");

        when(expenseWithUser.getExpense()).thenReturn(expense);
        when(expenseWithUser.getUser()).thenReturn(user);
        when(expenseWithUser.getApproval()).thenReturn(approval);

        when(approval.getStatus()).thenReturn("PENDING");
        when(approval.getReviewer()).thenReturn(null);
        when(approval.getComment()).thenReturn(null);
        when(approval.getReviewDate()).thenReturn(null);

        List<ExpenseWithUser> expenses = Collections.singletonList(expenseWithUser);

        // Act
        String result = expenseService.generateCsvReport(expenses);

        // Assert
        String expected = "Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n"
                + "2,alice,50.0,Lunch,2025-01-03,PENDING,,,\n";

        assertEquals(expected, result);
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify successful expense approval")
    @Severity(SeverityLevel.CRITICAL)
    void approveExpense_SuccessfulApproval_ReturnsTrue() {
        when(approvalRepository.updateApprovalStatus(123, "approved", 456, "Looks good"))
                .thenReturn(true);

        boolean result = expenseService.approveExpense(123, 456, "Looks good");

        assertTrue(result);
        verify(approvalRepository).updateApprovalStatus(123, "approved", 456, "Looks good");
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify failed expense approval")
    @Severity(SeverityLevel.NORMAL)
    void approveExpense_FailedApproval_ReturnsFalse() {
        when(approvalRepository.updateApprovalStatus(999, "approved", 456, "Test"))
                .thenReturn(false);

        boolean result = expenseService.approveExpense(999, 456, "Test");

        assertFalse(result);
        verify(approvalRepository).updateApprovalStatus(999, "approved", 456, "Test");
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify database error handling during approval")
    @Severity(SeverityLevel.CRITICAL)
    void approveExpense_DatabaseError_ThrowsRuntimeException() {
        when(approvalRepository.updateApprovalStatus(999, "approved", 100, "Approved"))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> expenseService.approveExpense(999, 100, "Approved"));

        verify(approvalRepository).updateApprovalStatus(999, "approved", 100, "Approved");
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify successful expense denial")
    @Severity(SeverityLevel.CRITICAL)
    void denyExpense_SuccessfulDenial_ReturnsTrue() {
        when(approvalRepository.updateApprovalStatus(123, "denied", 456, "Insufficient docs"))
                .thenReturn(true);

        boolean result = expenseService.denyExpense(123, 456, "Insufficient docs");

        assertTrue(result);
        verify(approvalRepository).updateApprovalStatus(123, "denied", 456, "Insufficient docs");
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify failed expense denial")
    @Severity(SeverityLevel.NORMAL)
    void denyExpense_FailedDenial_ReturnsFalse() {
        when(approvalRepository.updateApprovalStatus(999, "denied", 456, "Test"))
                .thenReturn(false);

        boolean result = expenseService.denyExpense(999, 456, "Test");

        assertFalse(result);
        verify(approvalRepository).updateApprovalStatus(999, "denied", 456, "Test");
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify database error handling during denial")
    @Severity(SeverityLevel.CRITICAL)
    void denyExpense_DatabaseError_ThrowsRuntimeException() {
        when(approvalRepository.updateApprovalStatus(999, "denied", 100, "Denied"))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> expenseService.denyExpense(999, 100, "Denied"));

        verify(approvalRepository).updateApprovalStatus(999, "denied", 100, "Denied");
    }
}
