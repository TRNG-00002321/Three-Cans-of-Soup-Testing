package com.revature.Api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.repository.DatabaseConnection;
import com.revature.repository.User;
import com.revature.repository.UserRepository;
import com.revature.service.AuthenticationService;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class ExpenseControllerTest {

    private static AuthenticationService authService;
    private final String BASE_URL = "http://localhost:5001";
    private static String manager_token;
    private static String employee_token;

    @BeforeAll
    public static void init() {
        authService = new AuthenticationService(new UserRepository(new DatabaseConnection()));
        manager_token = authService.createJwtToken(
                new User(1, "manager1", "password123", "Manager")
        );

        employee_token = authService.createJwtToken(
                new User(1, "employee1", "password123", "Employee")
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        DummyDataLoader dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
    }

    @Test
    public void get_expenses_with_valid_manager_auth_sucess() {
        given()
                .cookie("jwt", manager_token)
                .when()
                .get(BASE_URL + "/api/expenses")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("count", is(2))
                .body("data.size()", is(2))
                .body("data.expense.id", hasItems(4, 5));
    }

    @Test
    public void get_expenses_with_valid_manager_no_auth_fail() {
        given()
                .when()
                .get(BASE_URL + "/api/expenses")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("count", is(2))
                .body("data.size()", is(2))
                .body("data.expense.id", hasItems(4, 5));
    }

    @Test
    void getPendingExpenseValidAuthReturns200() {
        String jwt = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"manager1\", \"password\": \"password123\"}")
                .post(BASE_URL + "/api/auth/login")
                .getCookie("jwt");

        given()
                .cookie("jwt", jwt)
                .when()
                .get(BASE_URL + "/api/expenses/pending")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }
}
