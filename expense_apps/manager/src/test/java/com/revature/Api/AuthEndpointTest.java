package com.revature.Api;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;

public class AuthEndpointTest {

    private final String BASE_URL = "http://localhost:5001";

    @BeforeAll
    public static void setUp() throws Exception {
        DummyDataLoader dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
    }

    @Test
    void test_status_with_no_token() {
        given()
                .when()
                .get(BASE_URL + "/api/auth/status")
                .then()
                .statusCode(200)
                .body("authenticated", is(false));
    }

    @Test
    void test_logout_with_no_token() {
        given()
                .when()
                .post(BASE_URL + "/api/auth/logout")
                .then()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    void test_login_with_malformed_fields() {
        given()
                .body("{\"fail\":}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(400)
                .body("success", is(false))
                .body("error", is("Invalid request format"));
    }

    @Test
    void test_login_with_null_fields() {
        given()
                .body("{\"username\":null, \"password\":null}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(400)
                .body("success", is(false))
                .body("error", is("Username and password are required"));
    }

    @ParameterizedTest
    @CsvSource({
        "invalid_user, invalid_password",
        "invalid_user, password123",
        "manager1, invalid_password"
    })
    void test_login_with_invalid_credentials(String username, String password) {
        given()
                .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false));
    }

    @Test
    void test_login_with_incorrect_role() {
        given()
                .body("{\"username\":\"employee1\", \"password\":\"password123\"}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", is(false));
    }

    @Test
    void test_login_with_valid_credentials() {
        given()
                .body("{\"username\":\"manager1\", \"password\":\"password123\"}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("user.username", is("manager1"))
                .body("user.role", is("Manager"));
    }

    @Test
    void test_status_with_valid_credentials() {
        String jwt = given()
                .body("{\"username\":\"manager1\", \"password\":\"password123\"}")
                .post(BASE_URL + "/api/auth/login")
                .getCookie("jwt");

        given()
                .cookie("jwt", jwt)
                .when()
                .get(BASE_URL + "/api/auth/status")
                .then()
                .statusCode(200)
                .body("authenticated", is(true))
                .body("user.username", is("manager1"))
                .body("user.role", is("Manager"));

    }

    @Test
    void test_logout_with_valid_credentials() {
        io.restassured.filter.cookie.CookieFilter cookieFilter = new io.restassured.filter.cookie.CookieFilter();

        // Login
        given()
                .filter(cookieFilter)
                .contentType(io.restassured.http.ContentType.JSON)
                .body("{\"username\":\"manager1\", \"password\":\"password123\"}")
                .when()
                .post(BASE_URL + "/api/auth/login")
                .then()
                .statusCode(200);

        // Logout
        given()
                .filter(cookieFilter)
                .when()
                .post(BASE_URL + "/api/auth/logout")
                .then()
                .statusCode(200)
                .body("success", is(true));

        // Verify status is unauthenticated
        given()
                .filter(cookieFilter)
                .when()
                .get(BASE_URL + "/api/auth/status")
                .then()
                .statusCode(200)
                .body("authenticated", is(false));
    }

}
