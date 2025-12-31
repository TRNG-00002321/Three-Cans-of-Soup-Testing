package com.revature.Api;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.Api.DummyDataLoader;
import com.revature.Main;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ReportControllerTest {

    private final String BASE_URL = "http://localhost:5001";

    @BeforeEach
    public void setUp() throws Exception {
        DummyDataLoader dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
    }

    @Test
    public void generateAllExpenseReport() {
        String managerJwtCookie = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"manager1\", \"password\": \"password123\"}")
                .post(BASE_URL + "/api/auth/login")
                .getCookie("jwt");

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
