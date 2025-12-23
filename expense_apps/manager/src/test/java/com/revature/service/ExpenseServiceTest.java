package com.revature.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import com.revature.repository.*;
import com.revature.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;
    // class that contains generateCsvReport()

    @Mock
    private ExpenseWithUser expenseWithUser;

    @Mock
    private Expense expense;

    @Mock
    private User user;

    @Mock
    private Approval approval;

    @BeforeEach
    void setUp() {
        when(expenseWithUser.getExpense()).thenReturn(expense);
        when(expenseWithUser.getUser()).thenReturn(user);
        when(expenseWithUser.getApproval()).thenReturn(approval);
    }

    @Test
    void generateCsvReportTestAllFields() {
        // Arrange
        when(expense.getId()).thenReturn(1);
        when(user.getUsername()).thenReturn("john_doe");
        when(expense.getAmount()).thenReturn(123.45);
        when(expense.getDescription()).thenReturn("Taxi ride");
        when(expense.getDate()).thenReturn("2025-01-01");

        when(approval.getStatus()).thenReturn("APPROVED");
        when(approval.getReviewer()).thenReturn(10);
        when(approval.getComment()).thenReturn("Looks good");
        when(approval.getReviewDate()).thenReturn("2025-01-02");

        List<ExpenseWithUser> expenses = Collections.singletonList(expenseWithUser);

        // Act
        String result = expenseService.generateCsvReport(expenses);

        // Assert
        String expected =
                "Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n" +
                        "1,john_doe,123.45,Taxi ride,2025-01-01,APPROVED,10,Looks good,2025-01-02\n";

        assertEquals(expected, result);
    }

    @Test
    void generateCsvReportTestPending() {
        // Arrange
        when(expense.getId()).thenReturn(2);
        when(user.getUsername()).thenReturn("alice");
        when(expense.getAmount()).thenReturn(50.00);
        when(expense.getDescription()).thenReturn("Lunch");
        when(expense.getDate()).thenReturn("2025-01-03");

        when(approval.getStatus()).thenReturn("PENDING");
        when(approval.getReviewer()).thenReturn(null);
        when(approval.getComment()).thenReturn(null);
        when(approval.getReviewDate()).thenReturn(null);

        List<ExpenseWithUser> expenses = Collections.singletonList(expenseWithUser);

        // Act
        String result = expenseService.generateCsvReport(expenses);

        // Assert
        String expected =
                "Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n" +
                        "2,alice,50.0,Lunch,2025-01-03,PENDING,,,\n";

        assertEquals(expected, result);
    }


}

