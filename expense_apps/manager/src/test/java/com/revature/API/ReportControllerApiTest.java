package com.revature.API;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.Main;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ReportControllerApiTest {

    // private static DatabaseConnection testDbConnection;
    private static String managerJwtCookie;
    private static final int PORT = 5001;

    @BeforeAll
    public static void setupAll() throws InterruptedException, SQLException, IOException {

        TestDatabaseSetup.initializeTestDatabase();
        Path db_path = TestDatabaseSetup.getTestDbPath();

        System.setProperty("databasePath", db_path.toString());

        Thread server = new Thread(() -> {
            try {
                Main.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "manager-server-thread");
        server.setDaemon(true);
        server.start();

        waitForPort("localhost", PORT, 5000);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = PORT;
    
        String requestBody = """
                {
                    "username": "manager1",
                    "password": "admin123"
                }
                """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        managerJwtCookie = response.getCookie("jwt");
        Assertions.assertNotNull(managerJwtCookie, "JWT cookie should be set");
    }

    @AfterAll
    static void teardownAll(){
        TestDatabaseSetup.cleanup();
    }

    private static void waitForPort(String host, int port, int timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            try (Socket s = new Socket(host, port)) {
                return;
            } catch (IOException ignored) {
                Thread.sleep(200);
            }
        }
        throw new IllegalStateException("Server did not start on " + host + ":" + port);
    }

    @Test
    public void generateAllExpenseReport() {

        String responseBody = given().cookie("jwt", managerJwtCookie)
                .when().get("/api/reports/expenses/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .extract().body().asString();
        
        Assertions.assertTrue(responseBody.contains("Business lunch"));
        Assertions.assertTrue(responseBody.contains("Travel expense"));
        Assertions.assertTrue(responseBody.contains("Office supplies"));
    }

    @Test
    public void generateEmployeeExpensesReport() {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("employeeId", 1)
                .when().get("/api/reports/expenses/employee/{employeeId}/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Business lunch"))
                .body(org.hamcrest.Matchers.containsString("Travel expense"))
                .body(org.hamcrest.Matchers.containsString("Office supplies"));
    }

    @Test
    public void generateCategoryExpensesReport() {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("category", "Travel")
                .when().get("/api/reports/expenses/category/{category}/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Travel expense"));
    }

    @Test
    public void generateDateRangeExpensesReport() {
        given().cookie("jwt", managerJwtCookie)
                .queryParam("startDate", "2024-12-01")
                .queryParam("endDate", "2024-12-05")
                .when().get("/api/reports/expenses/daterange/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Business lunch"))
                .body(org.hamcrest.Matchers.containsString("Travel expense"))
                .body(org.hamcrest.Matchers.containsString("Client meeting"));
    }

    @Test
    public void generatePendingExpensesReport() {
        given().cookie("jwt", managerJwtCookie)
                .when().get("/api/reports/expenses/pending/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Business lunch"))
                .body(org.hamcrest.Matchers.containsString("Client meeting"))
                .body(org.hamcrest.Matchers.containsString("Team dinner"));
    }

    @Test
    public void generateDateRangeExpensesReport_InvalidFormat() {
        given().cookie("jwt", managerJwtCookie)
                .queryParam("startDate", "12-01-2024")
                .queryParam("endDate", "2024-12-05")
                .when().get("/api/reports/expenses/daterange/csv")
                .then()
                .statusCode(400);
    }

    @Test
    public void generateEmployeeExpensesReport_InvalidId() {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("employeeId", "abc")
                .when().get("/api/reports/expenses/employee/{employeeId}/csv")
                .then()
                .statusCode(400);
    }

}
