package com.revature.Api;

import com.revature.Main;

import com.revature.repository.DatabaseConnection;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.restassured.RestAssured;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class ExpenseApiTest {

    private static final Logger log = LoggerFactory.getLogger(ExpenseApiTest.class);

    private static boolean server_started = false;

    static DatabaseConnection databaseConnection;


    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 5001;
        if (!server_started) {
            System.setProperty("TestMode", "true");
            System.setProperty("databasePath", "src/test/java/com/revature/Api/expense_manager_test.db");
            databaseConnection = new DatabaseConnection();

            Main.main(new String[]{});
            server_started = true;
        }
    }

    @AfterAll
    static void tearDown() {
        if (server_started) {
            System.setProperty("TestMode", "false");
            System.exit(0);
            server_started = false;
        }
    }

//    void addExpensesAndPendingApprovalsWithFile(String txtFile) {
//        try(InputStream is = getClass().getClassLoader().getResourceAsStream(txtFile);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
//            String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] parts = line.split(",");
//                    int id = Integer.parseInt(parts[0]);
//                    int employee = Integer.parseInt(parts[1]);
//                    double amount = Double.parseDouble(parts[2]);
//                    String description = parts[3];
//                    String date = parts[4];
//
//                    addExpenseAndPendingApproval(id, employee, amount, description, date);
//
//                }
//        } catch (IOException e) {
//            fail("File not read :: " + e.getMessage());
//        } catch (SQLException e) {
//            fail("Expense not added :: " + e.getMessage());
//        }
//    }

    void addExpenseAndPendingApproval(int id, int employee, double amount, String description, String date) throws SQLException {
        String expenseSql = "INSERT INTO expenses (id, user_id, amount, description, date) VALUES (?, ?, ?, ?, ?)";
        String approvalSql = "INSERT INTO approvals (expense_id, status) VALUES (?, 'pending')";

        try (Connection conn = databaseConnection.getConnection()){
            PreparedStatement stmt = conn.prepareStatement(expenseSql);
                stmt.setInt(1, id);
                stmt.setInt(2, employee);
                stmt.setDouble(3, amount);
                stmt.setString(4, description);
                stmt.setString(5, date);
                stmt.executeUpdate();

            PreparedStatement stmt2 = conn.prepareStatement(approvalSql);
                stmt2.setInt(1, id);
                stmt2.executeUpdate();

        }
    }

    void resetDatabase() throws SQLException {
        try (Connection conn = databaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM expenses");
            stmt.executeUpdate();

            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM approvals");
            stmt2.executeUpdate();
        }
    }


//    @BeforeEach
//    void seedDatabase() {
//        addExpensesAndPendingApprovalsWithFile("expenseExamples.txt");
//    }
//
//    @AfterEach
//    void cleanDatabase() throws SQLException {
//        resetDatabase();
//    }

    @Test
    void connectionTest(){
        given()
        .when()
            .get("http://localhost:5001/health")
        .then()
            .statusCode(200);
    }

    @Test
    void getPendingExpenseValidAuthReturns200() throws SQLException {
        addExpenseAndPendingApproval(1, 1, 123, "something", "12-01-2025");
        addExpenseAndPendingApproval(2, 1, 123, "something", "12-01-2025");

        String jwt = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"manager1\", \"password\": \"password123\"}")
                .post("/api/auth/login")
                .getCookie("jwt");

        given()
            .cookie("jwt", jwt)
        .when()
            .get("/api/expenses/pending")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", hasSize(2))
            .body("count", equalTo(2));

        resetDatabase();
    }

    @Test
    void getPendingExpenseInvalidAuthReturns401(){
        given()

        .when()
            .get("/api/expenses/pending")
        .then()
            .statusCode(401);
    }

    @Test
    void approveExpenseSuccess() {

    }
    @Test
    void denyExpenseSuccess() {

    }

    @Test
    void getAllExpensesValidAuth() {

    }
    @Test
    void getAllExpensesInvalidAuth() {

    }

    @Test
    void getExpenseByEmployeeReturns200() {
        String jwt = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"manager1\", \"password\": \"password123\"}")
                .post("/api/auth/login")
                .getCookie("jwt");

        given()
            .cookie("jwt", jwt)
        .when()
            .get("/api/expenses/employee/1")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("employeeId", equalTo(1));
    }

}
