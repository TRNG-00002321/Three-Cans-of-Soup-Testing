package com.revature.Unit.api;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.api.ReportController;
import com.revature.repository.ExpenseWithUser;
import com.revature.service.ExpenseService;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.validation.Validator;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock //creates the mocked UserRepository
    private ExpenseService expenseService;
    @Mock
    private Context ctx;
    @Mock
    private Validator<Integer> employeeIdValidator;
    @InjectMocks
    private ReportController reportController;

    private List<ExpenseWithUser> mockExpenses;
    String mockCsv;

    @BeforeEach
    public void setup() {
        mockExpenses = List.of(
                new ExpenseWithUser(),
                new ExpenseWithUser()
        );

        mockCsv = "id,amount,user\n1,100,Alice";
    }

    @Test
    @DisplayName("Generate All Expenses Report Success")
    public void generateAllExpensesReportTestHappy() {

        when(expenseService.getAllExpenses()).thenReturn(mockExpenses);
        when(expenseService.generateCsvReport(mockExpenses)).thenReturn(mockCsv);

        // Act
        reportController.generateAllExpensesReport(ctx);

        // Assert
        verify(expenseService).getAllExpenses();
        verify(expenseService).generateCsvReport(mockExpenses);

        verify(ctx).contentType("text/csv");
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"all_expenses_report.csv\""
        );
        verify(ctx).result(mockCsv);
    }

    @Test
    @DisplayName("Generate All Expenses Report Exception")
    public void generateAllExpensesReportTestException() {
        when(expenseService.getAllExpenses()).thenThrow(new InternalServerErrorResponse());

        assertThrows(InternalServerErrorResponse.class, () -> reportController.generateAllExpensesReport(ctx));

        verify(expenseService).getAllExpenses();
        verifyNoMoreInteractions(ctx);
    }

    @Test
    @DisplayName("Generate Employee Expenses Report Success")
    public void generateEmployeeExpensesReportTestHappy() {
        int employeeId = 1;

        when(ctx.pathParamAsClass("employeeId", Integer.class))
                .thenReturn(employeeIdValidator);
        when(employeeIdValidator.get()).thenReturn(employeeId);

        when(expenseService.getExpensesByEmployee(employeeId))
                .thenReturn(mockExpenses);
        when(expenseService.generateCsvReport(mockExpenses))
                .thenReturn(mockCsv);

        // Act
        reportController.generateEmployeeExpensesReport(ctx);

        // Assert
        verify(ctx).contentType("text/csv");
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"employee_" + employeeId + "_expenses_report.csv\""
        );
        verify(ctx).result(mockCsv);
        verify(expenseService).getExpensesByEmployee(employeeId);
        verify(expenseService).generateCsvReport(mockExpenses);
    }

    @Test
    @DisplayName("Generate Employee Expenses Report Bad Request Exception")
    public void generateEmployeeExpensesReportTestBadRequestException() {
        when(ctx.pathParamAsClass("employeeId", Integer.class))
                .thenThrow(new NumberFormatException("Invalid number"));

        // Act + Assert
        assertThrows(
                BadRequestResponse.class,
                () -> reportController.generateEmployeeExpensesReport(ctx)
        );

        verifyNoInteractions(expenseService);

    }

    @Test
    @DisplayName("Generate Employee Expenses Report Internal Server Error")
    public void generateEmployeeExpensesReportTestInternalServerErrorException() {
        int employeeId = 10;

        when(ctx.pathParamAsClass("employeeId", Integer.class))
                .thenReturn(employeeIdValidator);
        when(employeeIdValidator.get()).thenReturn(employeeId);

        when(expenseService.getExpensesByEmployee(employeeId))
                .thenThrow(new RuntimeException("Database down"));

        // Act + Assert
        assertThrows(
                InternalServerErrorResponse.class,
                () -> reportController.generateEmployeeExpensesReport(ctx)
        );
    }

    @Test
    @DisplayName("Generate Category Expenses Report Success")
    public void generateCategoryExpensesReportTestHappy() {
        String category = "food";

        when(ctx.pathParam("category"))
                .thenReturn(category);

        when(expenseService.getExpensesByCategory(category))
                .thenReturn(mockExpenses);
        when(expenseService.generateCsvReport(mockExpenses))
                .thenReturn(mockCsv);

        // Act
        reportController.generateCategoryExpensesReport(ctx);

        verify(ctx).contentType("text/csv");
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"category_" + category + "_expenses_report.csv\""
        );
        verify(ctx).result(mockCsv);
        verify(expenseService).getExpensesByCategory(category);
        verify(expenseService).generateCsvReport(mockExpenses);
    }

    @Test
    @DisplayName("Generate Category Expenses Report Clean Name")
    void generateCategoryExpensesReportTestCleanFilename() {
        // Arrange
        String category = "food & drinks!";
        String safeCategory = "food___drinks_";

        when(ctx.pathParam("category")).thenReturn(category);
        when(expenseService.getExpensesByCategory(category))
                .thenReturn(List.of());
        when(expenseService.generateCsvReport(any()))
                .thenReturn("");

        // Act
        reportController.generateCategoryExpensesReport(ctx);

        // Assert
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"category_" + safeCategory + "_expenses_report.csv\""
        );
    }

    @Test
    @DisplayName("Generate Category Expenses Report Bad Request Exception")
    public void generateCategoryExpensesReportTestBadRequestException() {
        when(ctx.pathParam("category")).thenReturn("   ");
        // Act + Assert
        assertThrows(
                BadRequestResponse.class,
                () -> reportController.generateCategoryExpensesReport(ctx)
        );

        verifyNoInteractions(expenseService);

    }

    @Test
    @DisplayName("Generate Category Expenses Report Internal Server Error")
    public void generateCategoryExpensesReportTestInternalServerErrorException() {
        String category = "bad category";

        when(ctx.pathParam("category"))
                .thenReturn(category);
        when(expenseService.getExpensesByCategory(category)).thenThrow(new RuntimeException("Database error"));

        // Act + Assert
        assertThrows(
                InternalServerErrorResponse.class,
                () -> reportController.generateCategoryExpensesReport(ctx)
        );
    }

    @Test
    @DisplayName("Generate By Date Expenses Report Success")
    void generateDateRangeExpensesReport_success() {
        // Arrange
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        when(ctx.queryParam("startDate")).thenReturn(startDate);
        when(ctx.queryParam("endDate")).thenReturn(endDate);

        when(expenseService.getExpensesByDateRange(startDate, endDate))
                .thenReturn(mockExpenses);
        when(expenseService.generateCsvReport(mockExpenses))
                .thenReturn(mockCsv);

        // Act
        reportController.generateDateRangeExpensesReport(ctx);

        // Assert
        verify(ctx).contentType("text/csv");
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"expenses_" + startDate + "_to_" + endDate + "_report.csv\""
        );
        verify(ctx).result(mockCsv);

        verify(expenseService).getExpensesByDateRange(startDate, endDate);
        verify(expenseService).generateCsvReport(mockExpenses);
    }

    @Test
    @DisplayName("Generate By Date Expenses Report Bad Request (Empty)")
    void generateDateRangeExpensesReportEmptyDateFormatThrowsBadRequest() {
        // Arrange
        when(ctx.queryParam("startDate")).thenReturn(null);
        when(ctx.queryParam("endDate")).thenReturn(null);

        // Act + Assert
        assertThrows(
                BadRequestResponse.class,
                () -> reportController.generateDateRangeExpensesReport(ctx)
        );

        verifyNoInteractions(expenseService);
    }

    @Test
    @DisplayName("Generate By Date Expenses Report Bad Request (Invalid Format)")
    void generateDateRangeExpensesReportInvalidDateFormatThrowsBadRequest() {
        // Arrange
        when(ctx.queryParam("startDate")).thenReturn("01-01-2024");
        when(ctx.queryParam("endDate")).thenReturn("2024/01/31");

        // Act + Assert
        assertThrows(
                BadRequestResponse.class,
                () -> reportController.generateDateRangeExpensesReport(ctx)
        );

        verifyNoInteractions(expenseService);
    }

    @Test
    @DisplayName("Generate By Date Expenses Report Internal Server Error")
    void generateDateRangeExpensesReportThrowsInternalServerError() {
        // Arrange
        String startDate = "2024-02-01";
        String endDate = "2024-02-28";

        when(ctx.queryParam("startDate")).thenReturn(startDate);
        when(ctx.queryParam("endDate")).thenReturn(endDate);

        when(expenseService.getExpensesByDateRange(startDate, endDate))
                .thenThrow(new RuntimeException("Database down"));

        // Act + Assert
        assertThrows(
                InternalServerErrorResponse.class,
                () -> reportController.generateDateRangeExpensesReport(ctx)
        );
    }

    @Test
    @DisplayName("Generate Pending Expenses Report Success")
    void generatePendingExpensesReport_success() {
        // Arrange
        List<ExpenseWithUser> mockExpenses = List.of(
                new ExpenseWithUser(),
                new ExpenseWithUser()
        );

        String mockCsv = "id,amount,user,status\n1,100,Alice,PENDING";

        when(expenseService.getPendingExpenses()).thenReturn(mockExpenses);
        when(expenseService.generateCsvReport(mockExpenses)).thenReturn(mockCsv);

        // Act
        reportController.generatePendingExpensesReport(ctx);

        // Assert
        verify(expenseService).getPendingExpenses();
        verify(expenseService).generateCsvReport(mockExpenses);

        verify(ctx).contentType("text/csv");
        verify(ctx).header(
                "Content-Disposition",
                "attachment; filename=\"pending_expenses_report.csv\""
        );
        verify(ctx).result(mockCsv);
    }

    @Test
    @DisplayName("Generate Pending Expenses Report Internal Server Error")
    void generatePendingExpensesReportThrowsInternalServerError() {
        // Arrange
        when(expenseService.getPendingExpenses())
                .thenThrow(new RuntimeException("DB error"));

        // Act + Assert
        assertThrows(
                InternalServerErrorResponse.class,
                () -> reportController.generatePendingExpensesReport(ctx)
        );

        verify(expenseService).getPendingExpenses();
        verifyNoInteractions(ctx);
    }
}
