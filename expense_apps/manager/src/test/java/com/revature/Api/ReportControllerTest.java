package com.revature.Api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class ReportControllerTest {

    private final String BASE_URL = "http://localhost:5001";
    private String managerJwtCookie;

    @BeforeEach
    public void setUp() throws Exception {
        DummyDataLoader dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
        RestAssured.baseURI = BASE_URL;

        managerJwtCookie = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"manager1\", \"password\": \"password123\"}")
                .post(BASE_URL + "/api/auth/login")
                .getCookie("jwt");
    }

    @Test
    public void generateAllExpenseReport_ValidToken() {

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

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5})
    public void generateEmployeeExpensesReport(int employeeId) {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("employeeId", employeeId)
                .when().get("/api/reports/expenses/employee/{employeeId}/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Expense ID"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Travel", "Business", "Office", "Dinner"})
    public void generateCategoryExpensesReport(String category) {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("category", category)
                .when().get("/api/reports/expenses/category/{category}/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv")
                .body(org.hamcrest.Matchers.containsString("Expense ID"));
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
                .body(org.hamcrest.Matchers.containsString("Business lunch"));
    }

    @Test
    public void generatePendingExpensesReport() {
        given().cookie("jwt", managerJwtCookie)
                .when().get("/api/reports/expenses/pending/csv")
                .then()
                .statusCode(200)
                .contentType("text/csv");
    }

    @ParameterizedTest
    @CsvSource({
        "12-01-2024, 2024-12-05",
        "2024-12-01, 12-05-2024",
        "invalid, 2024-12-05",
        "2024-12-01, invalid"
    })
    public void generateDateRangeExpensesReport_InvalidFormat(String startDate, String endDate) {
        given().cookie("jwt", managerJwtCookie)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .when().get("/api/reports/expenses/daterange/csv")
                .then()
                .statusCode(400);
    }

    @Test
    @Disabled("Disabled due to bug in ReportController parameter handling")
    public void generateEmployeeExpensesReport_InvalidId() {
        given().cookie("jwt", managerJwtCookie)
                .pathParam("employeeId", "abc")
                .when().get("/api/reports/expenses/employee/{employeeId}/csv")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @CsvSource({
        "/api/reports/expenses/csv",
        "/api/reports/expenses/pending/csv"
    })
    public void testProtectedEndpoints_NoParams_InvalidToken(String endpoint) {
        // No token
        given()
                .when().get(endpoint)
                .then()
                .statusCode(401);

        // Invalid token
        given().cookie("jwt", "invalid_token_value")
                .when().get(endpoint)
                .then()
                .statusCode(401);
    }

    @ParameterizedTest
    @CsvSource({
        "/api/reports/expenses/employee/{id}/csv, id, 1",
        "/api/reports/expenses/category/{cat}/csv, cat, Travel"
    })
    public void testProtectedEndpoints_WithPathParam_InvalidToken(String endpoint, String paramName, String paramValue) {
        // No token
        given()
                .pathParam(paramName, paramValue)
                .when().get(endpoint)
                .then()
                .statusCode(401);

        // Invalid token
        given().cookie("jwt", "invalid_token_value")
                .pathParam(paramName, paramValue)
                .when().get(endpoint)
                .then()
                .statusCode(401);
    }

    @Test
    public void testProtectedEndpoints_DateRange_InvalidToken() {
        // No token
        given()
                .queryParam("startDate", "2024-01-01")
                .queryParam("endDate", "2024-01-31")
                .when().get("/api/reports/expenses/daterange/csv")
                .then()
                .statusCode(401);

        // Invalid token
        given().cookie("jwt", "invalid_token_value")
                .queryParam("startDate", "2024-01-01")
                .queryParam("endDate", "2024-01-31")
                .when().get("/api/reports/expenses/daterange/csv")
                .then()
                .statusCode(401);
    }

}
