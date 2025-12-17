package com.revature.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.repository.Approval;
import com.revature.repository.Expense;
import com.revature.repository.ExpenseWithUser;
import com.revature.repository.User;
import com.revature.service.ExpenseService;

import io.javalin.http.Context;
import io.javalin.validation.Validator;

@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {

    Context ctxMock;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    public void setup() {
        ctxMock = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @AfterEach
    public void teardown() {
        ctxMock = null;
    }

    private List<ExpenseWithUser> getPendingExpensesStub() {
        ExpenseWithUser[] expenses = {
            new ExpenseWithUser(new Expense(), new User(), new Approval()),
            new ExpenseWithUser(new Expense(), new User(), new Approval()),
            new ExpenseWithUser(new Expense(), new User(), new Approval())
        };
        return Arrays.asList(expenses);
    }

    @Test
    public void getPendingExpenses_UpdatesContext() {
        // Arrange
        List<ExpenseWithUser> expenses = getPendingExpensesStub();
        when(expenseService.getPendingExpenses()).thenReturn(expenses);

        // Act
        expenseController.getPendingExpenses(ctxMock);

        // Assert
        Mockito.verify(expenseService, Mockito.times(1)).getPendingExpenses();
        Mockito.verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size()
        ));
    }

    @Test
    public void getPendingExpenses_ThrowsError() {
        // Arrange
        when(expenseService.getPendingExpenses()).thenThrow(RuntimeException.class);

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> expenseService.getPendingExpenses());
    }

    @Test
    public void getPendingExpenseByEmployee_InvalidId_ReturnsEmptyList() {
        // Arrange
        int employeeId = 999;
        List<ExpenseWithUser> expenses = new ArrayList<>();

        Validator<Integer> validatorMock = Mockito.mock(Validator.class);
        when(validatorMock.get()).thenReturn(employeeId);
        when(ctxMock.pathParamAsClass("employeeId", Integer.class)).thenReturn(validatorMock);
        when(expenseService.getExpensesByEmployee(employeeId)).thenReturn(expenses);

        // Act
        expenseController.getExpensesByEmployee(ctxMock);

        // Assert
        Mockito.verify(ctxMock, times(1)).pathParamAsClass("employeeId", Integer.class);
        Mockito.verify(validatorMock, times(1)).get();
        Mockito.verify(expenseService, times(1)).getExpensesByEmployee(employeeId);
        Mockito.verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size(),
                "employeeId", employeeId
        ));
    }

    @Test
    public void getPendingExpenseByEmployee_ValidId_ReturnsNonEmptyList() {
        // Arrange
        int employeeId = 999;
        List<ExpenseWithUser> expenses = getPendingExpensesStub();

        Validator<Integer> validatorMock = Mockito.mock(Validator.class);
        when(validatorMock.get()).thenReturn(employeeId);
        when(ctxMock.pathParamAsClass("employeeId", Integer.class)).thenReturn(validatorMock);
        when(expenseService.getExpensesByEmployee(employeeId)).thenReturn(expenses);

        // Act
        expenseController.getExpensesByEmployee(ctxMock);

        // Assert
        Mockito.verify(ctxMock, times(1)).pathParamAsClass("employeeId", Integer.class);
        Mockito.verify(validatorMock, times(1)).get();
        Mockito.verify(expenseService, times(1)).getExpensesByEmployee(employeeId);
        Mockito.verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size(),
                "employeeId", employeeId
        ));
    }

    @Test
    public void getPendingExpenseByEmployee_ThrowsError() {

    }
}
