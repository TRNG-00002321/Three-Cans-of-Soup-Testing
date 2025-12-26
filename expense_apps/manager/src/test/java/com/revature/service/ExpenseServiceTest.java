package com.revature.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.repository.Approval;
import com.revature.repository.ApprovalRepository;
import com.revature.repository.Expense;
import com.revature.repository.ExpenseRepository;
import com.revature.repository.ExpenseWithUser;
import com.revature.repository.User;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

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
    public void getPendingExpenses_ReturnsExpenseList() {
        // Arrange
        when(expenseRepository.findPendingExpensesWithUsers()).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getPendingExpenses();

        // Assert
        verify(expenseRepository, times(1)).findPendingExpensesWithUsers();
    }

    @Test
    public void getPendingExpenses_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findPendingExpensesWithUsers()).thenThrow(new RuntimeException("Error finding pending expenses: "));

        // Act
        Executable action = () -> expenseService.getPendingExpenses();

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding pending expenses"));
        verify(expenseRepository, times(1)).findPendingExpensesWithUsers();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 99, 453})
    public void getExpensesByEmployee_ReturnsExpenseList(int employeeId) {

        // Arrange
        when(expenseRepository.findExpensesByUser(employeeId)).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getExpensesByEmployee(employeeId);

        // Assert
        verify(expenseRepository, times(1)).findExpensesByUser(employeeId);
    }

    @Test
    public void getExpenseByEmployee_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findExpensesByUser(anyInt())).thenThrow(new RuntimeException("Error finding expenses for user: "));

        // Act
        Executable action = () -> expenseService.getExpensesByEmployee(anyInt());

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding expenses for user"));
        verify(expenseRepository, times(1)).findExpensesByUser(anyInt());

    }

    @Test
    public void getAllExpenses_ReturnsExpenseList() {
        // Arrange
        when(expenseRepository.findAllExpensesWithUsers()).thenReturn(getListOfExpensesWithUser());

        // Act
        expenseService.getAllExpenses();

        // Assert
        verify(expenseRepository, times(1)).findAllExpensesWithUsers();
    }

    @Test
    public void getAllExpenses_ThrowsRuntimeException() {
        // Arrange
        when(expenseRepository.findAllExpensesWithUsers()).thenThrow(new RuntimeException("Error finding expenses for user: "));

        // Act
        Executable action = () -> expenseService.getAllExpenses();

        // Assert
        RuntimeException rte = Assertions.assertThrows(RuntimeException.class, action);
        assertTrue(rte.getMessage().contains("Error finding expenses for user"));
        verify(expenseRepository, times(1)).findAllExpensesWithUsers();
    }

}
