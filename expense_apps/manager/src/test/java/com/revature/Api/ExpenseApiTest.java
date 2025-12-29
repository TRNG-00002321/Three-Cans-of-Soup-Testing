package com.revature.Api;

import com.revature.api.ExpenseController;
import com.revature.service.ExpenseService;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.restassured.RestAssured;
import io.restassured.RestAssured.*;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
public class ExpenseApiTest {

    private static final Logger log = LoggerFactory.getLogger(ExpenseApiTest.class);

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:5001/";
    }

    //Don't forget to run main first
    @Test
    void connectionTest(){
        given()
        .when()
        .get("http://localhost:5001/health")
        .then()
        .statusCode(200);
    }

    @Test
    void getPendingExpenseValidAuthReturns200() {
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
            .statusCode(200);
    }

    @Test
    void getPendingExpenseInvalidAuthReturns401() {
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
