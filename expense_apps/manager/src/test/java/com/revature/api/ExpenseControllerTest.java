package com.revature.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.repository.Approval;
import com.revature.repository.Expense;
import com.revature.repository.ExpenseWithUser;
import com.revature.repository.User;
import com.revature.service.ExpenseService;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.Validator;

/* 
TODO: Implement ArgumentCaptor for Map Objects:
    Mockito.verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size()
        ));
 */
@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {

    Context ctxMock;
    private Validator<Integer> validatorMock;
    private MockedStatic<AuthenticationMiddleware> authMiddlewareMock;
    private User mockManager;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    @BeforeEach
    public void setup() {
        ctxMock = mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        validatorMock = mock(Validator.class);

        authMiddlewareMock = Mockito.mockStatic(AuthenticationMiddleware.class);
        mockManager = new User();
        mockManager.setId(10);
        authMiddlewareMock.when(() -> AuthenticationMiddleware.getAuthenticatedManager(ctxMock))
                .thenReturn(mockManager);
    }

    @AfterEach
    public void teardown() {
        ctxMock = null;
        validatorMock = null;
        if (authMiddlewareMock != null) {
            authMiddlewareMock.close();
        }
    }

    private List<ExpenseWithUser> getListOfExpensesWithUser() {
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
        List<ExpenseWithUser> expenses = getListOfExpensesWithUser();
        when(expenseService.getPendingExpenses()).thenReturn(expenses);

        // Act
        expenseController.getPendingExpenses(ctxMock);

        // Assert
        verify(expenseService, Mockito.times(1)).getPendingExpenses();
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size()));
    }

    @Test
    public void getPendingExpenses_ThrowsInternalServerErrorResponse() {
        // Arrange
        when(expenseService.getPendingExpenses()).thenThrow(RuntimeException.class);

        // Act
        Executable action = () -> expenseController.getPendingExpenses(ctxMock);

        // Assert
        InternalServerErrorResponse iser = Assertions.assertThrows(InternalServerErrorResponse.class, action);
        assertTrue(iser.getMessage().contains("Failed to retrieve pending expenses"));
        verify(expenseService, times(1)).getPendingExpenses();
    }

    @Test
    public void getAllExpenses_UpdatesContext() {
        // Arrange
        List<ExpenseWithUser> expenses = getListOfExpensesWithUser();
        when(expenseService.getAllExpenses()).thenReturn(expenses);

        // Act
        expenseController.getAllExpenses(ctxMock);

        // Assert
        verify(expenseService, Mockito.times(1)).getAllExpenses();
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size()));
    }

    @Test
    public void getAllExpenses_ThrowsInternalServerErrorResponse() {
        // Arrange
        when(expenseService.getAllExpenses()).thenThrow(RuntimeException.class);

        // Act
        Executable action = () -> expenseController.getAllExpenses(ctxMock);

        // Assert
        InternalServerErrorResponse iser = Assertions.assertThrows(InternalServerErrorResponse.class, action);
        assertTrue(iser.getMessage().contains("Failed to retrieve expenses"));
        verify(expenseService, times(1)).getAllExpenses();
    }

    @Test
    public void getExpenseByEmployee_ValidEmpId_UpdatesContext() {
        // Arrange
        int employeeId = 2;
        List<ExpenseWithUser> expenses = new ArrayList<>();

        when(validatorMock.get()).thenReturn(employeeId);
        when(ctxMock.pathParamAsClass("employeeId", Integer.class)).thenReturn(validatorMock);
        when(expenseService.getExpensesByEmployee(employeeId)).thenReturn(expenses);

        // Act
        expenseController.getExpensesByEmployee(ctxMock);

        // Assert
        verify(ctxMock, times(1)).pathParamAsClass("employeeId", Integer.class);
        verify(validatorMock, times(1)).get();
        verify(expenseService, times(1)).getExpensesByEmployee(employeeId);
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "data", expenses,
                "count", expenses.size(),
                "employeeId", employeeId));
    }

    @Disabled("BUG: Incorrect status code mapping; returns 500 instead of 404 on missing resource (EMS-35)")
    @Test
    public void getExpensesByEmployee_InvalidId_ThrowsNotFoundResponse() {
        // Arrange
        int employeeId = 999;
        when(expenseService.getExpensesByEmployee(employeeId)).thenThrow(RuntimeException.class);

        // Act
        Executable action = () -> expenseController.getExpensesByEmployee(ctxMock);

        // Assert
        NotFoundResponse nff = Assertions.assertThrows(NotFoundResponse.class, action);
        assertTrue(nff.getMessage().contains("Invalid employee ID"));
        verify(expenseService, times(1)).getExpensesByEmployee(employeeId);
    }

    @Test
    public void getExpenseByEmployee_MalformedCtx_ThrowsBadRequestResponse() {
        // Arrange
        when(ctxMock.pathParamAsClass("employeeId", Integer.class)).thenReturn(validatorMock);
        when(validatorMock.get()).thenThrow(NumberFormatException.class);

        // Act
        Executable action = () -> expenseController.getExpensesByEmployee(ctxMock);
        // Assert
        BadRequestResponse brr = Assertions.assertThrows(BadRequestResponse.class, action);
        assertTrue(brr.getMessage().contains("Invalid employee ID format"));
        verify(ctxMock, times(1)).pathParamAsClass("employeeId", Integer.class);
        verify(validatorMock, times(1)).get();

    }

    @Test
    public void getExpenseByEmployee_ThrowsInternalServerErrorResponse() {
        // Arrange
        int employeeId = 2;
        when(validatorMock.get()).thenReturn(employeeId);
        when(ctxMock.pathParamAsClass("employeeId", Integer.class)).thenReturn(validatorMock);
        when(expenseService.getExpensesByEmployee(employeeId))
                .thenThrow(new RuntimeException("Error finding expenses for user: " + employeeId));

        // Act
        Executable action = () -> expenseController.getExpensesByEmployee(ctxMock);

        // Assert
        InternalServerErrorResponse iser = Assertions.assertThrows(InternalServerErrorResponse.class, action);
        assertTrue(iser.getMessage().contains("Error finding expenses for user: " + employeeId));
        verify(ctxMock, times(1)).pathParamAsClass("employeeId", Integer.class);
        verify(expenseService, times(1)).getExpensesByEmployee(employeeId);

    }

    @Test
    public void approveExpense_ValidExpenseId_UpdatesContext() {
        int expenseId = 1;
        String comment = "Approved for processing";
        Map<String, Object> requestBody = Map.of("comment", comment);

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenReturn(requestBody);
        when(expenseService.approveExpense(expenseId, mockManager.getId(), comment)).thenReturn(true);

        expenseController.approveExpense(ctxMock);

        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
        verify(expenseService, times(1)).approveExpense(expenseId, mockManager.getId(), comment);
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "message", "Expense approved successfully"));
    }

    @Test
    public void approveExpense_ValidExpenseIdNoComment_UpdatesContext() {
        int expenseId = 1;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.approveExpense(expenseId, mockManager.getId(), null)).thenReturn(true);

        expenseController.approveExpense(ctxMock);

        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
        verify(expenseService, times(1)).approveExpense(expenseId, mockManager.getId(), null);
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "message", "Expense approved successfully"));
    }

    @Test
    public void approveExpense_MalformedExpenseId_ThrowsBadRequestResponse() {
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(validatorMock.get()).thenThrow(NumberFormatException.class);

        BadRequestResponse brr = Assertions.assertThrows(BadRequestResponse.class, () -> expenseController.approveExpense(ctxMock));

        assertTrue(brr.getMessage().contains("Invalid expense ID format"));
        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
    }

    @Test
    public void approveExpense_ServiceReturnsFalse_ThrowsNotFoundResponse() {
        int expenseId = 999;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.approveExpense(expenseId, mockManager.getId(), null)).thenReturn(false);

        NotFoundResponse nfr = Assertions.assertThrows(NotFoundResponse.class, () -> expenseController.approveExpense(ctxMock));

        assertTrue(nfr.getMessage().contains("Expense not found or could not be approved"));
        verify(expenseService, times(1)).approveExpense(expenseId, mockManager.getId(), null);
    }

    @Test
    public void approveExpense_ServiceThrowsException_ThrowsInternalServerErrorResponse() {
        int expenseId = 1;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.approveExpense(expenseId, mockManager.getId(), null))
                .thenThrow(new RuntimeException("Database error"));

        InternalServerErrorResponse iser = Assertions.assertThrows(InternalServerErrorResponse.class, () -> expenseController.approveExpense(ctxMock));

        assertTrue(iser.getMessage().contains("Failed to approve expense"));
        verify(expenseService, times(1)).approveExpense(expenseId, mockManager.getId(), null);
    }

    @Test
    public void denyExpense_ValidExpenseId_UpdatesContext() {
        int expenseId = 1;
        String comment = "Does not meet policy requirements";
        Map<String, Object> requestBody = Map.of("comment", comment);

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenReturn(requestBody);
        when(expenseService.denyExpense(expenseId, mockManager.getId(), comment)).thenReturn(true);

        expenseController.denyExpense(ctxMock);

        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
        verify(expenseService, times(1)).denyExpense(expenseId, mockManager.getId(), comment);
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "message", "Expense denied successfully"));
    }

    @Test
    public void denyExpense_ValidExpenseIdNoComment_UpdatesContext() {
        int expenseId = 1;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.denyExpense(expenseId, mockManager.getId(), null)).thenReturn(true);

        expenseController.denyExpense(ctxMock);

        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
        verify(expenseService, times(1)).denyExpense(expenseId, mockManager.getId(), null);
        verify(ctxMock, times(1)).json(Map.of(
                "success", true,
                "message", "Expense denied successfully"));
    }

    @Test
    public void denyExpense_MalformedExpenseId_ThrowsBadRequestResponse() {
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(validatorMock.get()).thenThrow(NumberFormatException.class);

        BadRequestResponse brr = Assertions.assertThrows(BadRequestResponse.class, () -> expenseController.denyExpense(ctxMock));

        assertTrue(brr.getMessage().contains("Invalid expense ID format"));
        verify(ctxMock, times(1)).pathParamAsClass("expenseId", Integer.class);
        verify(validatorMock, times(1)).get();
    }

    @Test
    public void denyExpense_ServiceReturnsFalse_ThrowsNotFoundResponse() {
        int expenseId = 999;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.denyExpense(expenseId, mockManager.getId(), null)).thenReturn(false);

        NotFoundResponse nfr = Assertions.assertThrows(NotFoundResponse.class, () -> expenseController.denyExpense(ctxMock));

        assertTrue(nfr.getMessage().contains("Expense not found or could not be denied"));
        verify(expenseService, times(1)).denyExpense(expenseId, mockManager.getId(), null);
    }

    @Test
    public void denyExpense_ServiceThrowsException_ThrowsInternalServerErrorResponse() {
        int expenseId = 1;

        when(validatorMock.get()).thenReturn(expenseId);
        when(ctxMock.pathParamAsClass("expenseId", Integer.class)).thenReturn(validatorMock);
        when(ctxMock.bodyAsClass(Map.class)).thenThrow(new RuntimeException("No body"));
        when(expenseService.denyExpense(expenseId, mockManager.getId(), null))
                .thenThrow(new RuntimeException("Database error"));

        InternalServerErrorResponse iser = Assertions.assertThrows(InternalServerErrorResponse.class, () -> expenseController.denyExpense(ctxMock));

        assertTrue(iser.getMessage().contains("Failed to deny expense"));
        verify(expenseService, times(1)).denyExpense(expenseId, mockManager.getId(), null);
    }
}
