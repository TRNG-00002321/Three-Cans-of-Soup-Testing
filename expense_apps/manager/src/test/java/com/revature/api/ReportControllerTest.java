package com.revature.api;

import com.revature.api.*;
import com.revature.repository.*;
import com.revature.service.*;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock //creates the mocked UserRepository
    private ExpenseService expenseService;
    @InjectMocks
    private ReportController reportController;

    private static List<ExpenseWithUser> allExpenses;
    private static List<ExpenseWithUser> employee1Expenses;
    private static String csvContent;
    @BeforeAll
    public static void setUp(){
        Expense expense1 = new Expense(1,1,50, "test expense", "2025-12-17");
        User user1 = new User(1,"Andrew", "Password123", "Employee");
        Approval approval1 = new Approval(1,1,"approved", 2, "comment about expense", "2025-12-17");

        Expense expense2 = new Expense(2,1,100, "test expense number 2", "2025-12-10");
        Approval approval2 = new Approval(2,2,"denied", 2, "comment about expense denial", "2025-12-16");
        User user2 = new User(3,"John", "123Password", "Employee");
        Expense expense3 = new Expense(3,3,25.50,"New user item", "2025-11-18");

        allExpenses = new ArrayList<ExpenseWithUser>();
        allExpenses.add(new ExpenseWithUser(expense1, user1, approval1));
        allExpenses.add(new ExpenseWithUser(expense2, user1, approval2));
        allExpenses.add(new ExpenseWithUser(expense3, user2, null));

        employee1Expenses = new ArrayList<ExpenseWithUser>();
        employee1Expenses.add(new ExpenseWithUser(expense1, user1, approval1));
        employee1Expenses.add(new ExpenseWithUser(expense2, user1, approval2));
        csvContent = """
                Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date
                """;

    }

    @Test
    @DisplayName("Generate All Expenses Report When Null")
    public void nullGenerateAllExpensesReportTest(){
        Mockito.when(expenseService.getAllExpenses()).thenReturn(null);
        Mockito.when(expenseService.generateCsvReport(allExpenses)).thenReturn("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n");

        Assertions.assertNull(expenseService.getAllExpenses());
        Assertions.assertEquals("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n",expenseService.generateCsvReport(allExpenses));

    }
    @Test
    @DisplayName("Generate All Expenses Report When Empty")
    public void emptyGenerateAllExpensesReportTest(){
        Mockito.when(expenseService.getAllExpenses()).thenReturn(new ArrayList<ExpenseWithUser>());
        Mockito.when(expenseService.generateCsvReport(allExpenses)).thenReturn("");

        Assertions.assertEquals(new ArrayList<>(),expenseService.getAllExpenses());
        Assertions.assertEquals("",expenseService.generateCsvReport(allExpenses));

    }

    @ParameterizedTest(name="{4} Report for {6}")
    @DisplayName("Generate ALl Expenses Report")
    @CsvSource({
            "0,1,1,50,test expense,2025-12-17,Andrew,Password123,Employee,1,approved,2,comment about expense,2025-12-17",
            "1,2,1,100,test expense number 2,2025-12-10,Andrew,Password123,Employee,2,denied,2,comment about expense denial,2025-12-16",
            "2,3,3,25.50,New user item,2025-11-18,John,123Password,Employee,,,,,"
    })
    public void generateAllExpensesReportTest(int expenseNumber,int expenseId, int userId, double amount,String description, String expenseDate, String username, String password,String role, Integer appId,String status, Integer reviewer, String comment, String reviewDate){
        Expense expense1 = new Expense(expenseId,userId,amount, description, expenseDate);
        User user1 = new User(userId,username, password, role);
        Approval approval1;
        if(appId!=null)
            approval1 = new Approval(appId,expenseId,status, reviewer, comment, reviewDate);
        else
            approval1 = null;
        ExpenseWithUser expectedExpense = new ExpenseWithUser(expense1,user1,approval1);

        String expectedCSV = """
                Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date
                1,Andrew,50,test expense,2025-12-17,approved,2,comment about expense,2025-12-17
                2,Andrew,100,test expense number 2,2025-12-10,denied,2,comment about expense denial,2025-12-16
                3,John,25.50,New user item,2025-11-18,pending,,,""";


        Mockito.when(expenseService.getAllExpenses()).thenReturn(allExpenses);
        Mockito.when(expenseService.generateCsvReport(allExpenses)).thenReturn(csvContent+"1,Andrew,50,test expense,2025-12-17,approved,2,comment about expense,2025-12-17\n" +
                "2,Andrew,100,test expense number 2,2025-12-10,denied,2,comment about expense denial,2025-12-16\n" +
                "3,John,25.50,New user item,2025-11-18,pending,,,");

        Assertions.assertNotNull(expenseService.getAllExpenses());
            Assertions.assertAll("Assert Expense is correct",
                    () -> Assertions.assertEquals(expectedExpense.getExpense().getId(),
                            expenseService.getAllExpenses().get(expenseNumber).getExpense().getId()),
                    () -> Assertions.assertEquals(expectedExpense.getExpense().getUserId(),
                            expenseService.getAllExpenses().get(expenseNumber).getExpense().getUserId()),
                    () -> Assertions.assertEquals(expectedExpense.getExpense().getDescription(),
                            expenseService.getAllExpenses().get(expenseNumber).getExpense().getDescription()),
                    () -> Assertions.assertEquals(expectedExpense.getExpense().getDate(),
                            expenseService.getAllExpenses().get(expenseNumber).getExpense().getDate())
            );
            Assertions.assertAll("Assert User is correct",
                    () -> Assertions.assertEquals(expectedExpense.getUser().getId(),
                            expenseService.getAllExpenses().get(expenseNumber).getUser().getId()),
                    () -> Assertions.assertEquals(expectedExpense.getUser().getUsername(),
                            expenseService.getAllExpenses().get(expenseNumber).getUser().getUsername()),
                    () -> Assertions.assertEquals(expectedExpense.getUser().getPassword(),
                            expenseService.getAllExpenses().get(expenseNumber).getUser().getPassword()),
                    () -> Assertions.assertEquals(expectedExpense.getUser().getRole(),
                            expenseService.getAllExpenses().get(expenseNumber).getUser().getRole())
            );
            if(expectedExpense.getApproval()!=null) {
                Assertions.assertAll("Assert Approval is correct",
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getId(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getId()),
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getExpenseId(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getExpenseId()),
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getStatus(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getStatus()),
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getReviewer(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getReviewer()),
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getComment(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getComment()),
                        () -> Assertions.assertEquals(expectedExpense.getApproval().getReviewDate(),
                                expenseService.getAllExpenses().get(expenseNumber).getApproval().getReviewDate())
                );
            }
            else
                Assertions.assertNull(expenseService.getAllExpenses().get(expenseNumber).getApproval());

        Assertions.assertEquals(expectedCSV,expenseService.generateCsvReport(allExpenses));
        //need to verify that file is being created
        //Mockito.verify(expenseService, Mockito.times(1)).generateCsvReport(expenses);
    }


    @Test
    @DisplayName("Generate Employee Expenses Report when Null")
    public void emptyGenerateEmployeeExpensesReportTest(){



        Mockito.when(expenseService.getExpensesByEmployee(999)).thenReturn(null);
        Mockito.when(expenseService.generateCsvReport(allExpenses)).thenReturn("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n");

        Assertions.assertNull(expenseService.getExpensesByEmployee(999));
        Assertions.assertEquals("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n",expenseService.generateCsvReport(allExpenses));


    }

    @Test
    @DisplayName("Generate Employee Expenses Report when empty")
    public void GenerateEmployeeExpensesReportTest(){
        Mockito.when(expenseService.getExpensesByEmployee(999)).thenReturn(new ArrayList<>());
        Mockito.when(expenseService.generateCsvReport(employee1Expenses)).thenReturn("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n");

        Assertions.assertEquals(new ArrayList<ExpenseWithUser>(),expenseService.getExpensesByEmployee(999));
        Assertions.assertEquals("Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date\n",expenseService.generateCsvReport(employee1Expenses));


    }

    @ParameterizedTest(name="{4} Report for {6}")
    @DisplayName("Generate Employee Expenses Report")
    @CsvSource({
            "0,1,1,50,test expense,2025-12-17,Andrew,Password123,Employee,1,approved,2,comment about expense,2025-12-17",
            "1,2,1,100,test expense number 2,2025-12-10,Andrew,Password123,Employee,2,denied,2,comment about expense denial,2025-12-16"
    })
    public void GenerateEmployeeExpensesTest(int expenseNumber,int expenseId, int userId, double amount,String description, String expenseDate, String username, String password,String role, Integer appId,String status, Integer reviewer, String comment, String reviewDate){

        Expense expense1 = new Expense(expenseId,userId,amount, description, expenseDate);
        User user1 = new User(userId,username, password, role);
        Approval approval1;
        if(appId!=null)
            approval1 = new Approval(appId,expenseId,status, reviewer, comment, reviewDate);
        else
            approval1 = null;
        ExpenseWithUser expectedExpense = new ExpenseWithUser(expense1,user1,approval1);



        String expectedCSV = """
                Expense ID,Employee,Amount,Description,Date,Status,Reviewer,Comment,Review Date
                1,Andrew,50,test expense,2025-12-17,approved,2,comment about expense,2025-12-17
                2,Andrew,100,test expense number 2,2025-12-10,denied,2,comment about expense denial,2025-12-16""";

        Mockito.when(expenseService.getExpensesByEmployee(1)).thenReturn(employee1Expenses);
        Mockito.when(expenseService.generateCsvReport(employee1Expenses)).thenReturn(csvContent+"1,Andrew,50,test expense,2025-12-17,approved,2,comment about expense,2025-12-17\n" +
                "2,Andrew,100,test expense number 2,2025-12-10,denied,2,comment about expense denial,2025-12-16");

        Assertions.assertNotNull(expenseService.getAllExpenses());
        Assertions.assertAll("Assert Expense is correct",
                () -> Assertions.assertEquals(expectedExpense.getExpense().getId(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getExpense().getId()),
                () -> Assertions.assertEquals(expectedExpense.getExpense().getUserId(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getExpense().getUserId()),
                () -> Assertions.assertEquals(expectedExpense.getExpense().getDescription(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getExpense().getDescription()),
                () -> Assertions.assertEquals(expectedExpense.getExpense().getDate(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getExpense().getDate())
        );
        Assertions.assertAll("Assert User is correct",
                () -> Assertions.assertEquals(expectedExpense.getUser().getId(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getUser().getId()),
                () -> Assertions.assertEquals(expectedExpense.getUser().getUsername(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getUser().getUsername()),
                () -> Assertions.assertEquals(expectedExpense.getUser().getPassword(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getUser().getPassword()),
                () -> Assertions.assertEquals(expectedExpense.getUser().getRole(),
                        expenseService.getExpensesByEmployee(1).get(expenseNumber).getUser().getRole())
        );
        if(expectedExpense.getApproval()!=null) {
            Assertions.assertAll("Assert Approval is correct",
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getId(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getId()),
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getExpenseId(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getExpenseId()),
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getStatus(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getStatus()),
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getReviewer(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getReviewer()),
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getComment(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getComment()),
                    () -> Assertions.assertEquals(expectedExpense.getApproval().getReviewDate(),
                            expenseService.getExpensesByEmployee(1).get(expenseNumber).getApproval().getReviewDate())
            );
        }
        else
            Assertions.assertNull(expenseService.getAllExpenses().get(expenseNumber).getApproval());


        Assertions.assertEquals(expectedCSV,expenseService.generateCsvReport(employee1Expenses));

    }

}
