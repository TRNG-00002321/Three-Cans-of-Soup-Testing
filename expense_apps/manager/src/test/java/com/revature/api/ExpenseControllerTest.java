package com.revature.api;

import com.revature.repository.Expense;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;



public class ExpenseControllerTest {

    protected static CookieFilter cookieFilter;
    static DummyDataLoader dataLoader;

    @BeforeAll
    public static void BaseSetUp(){

        RestAssured.baseURI = "http://localhost:5001";
        cookieFilter = new CookieFilter();

    }

    @BeforeEach
    void login() {
        given()
                .filter(cookieFilter)
                .contentType("application/json")
                .body("""
                {
                  "username": "manager1",
                  "password": "password123"
                }
            """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200);
    }

    @BeforeEach
    public void setUp() throws Exception {
        dataLoader = new DummyDataLoader();
        dataLoader.restoreDatabase();
    }



    @Test
    public void dummyTest() {
        // Placeholder test to ensure setup runs
        System.out.println("test");
    }

    @Test
    public void denyExpenseHappyTest() {
        Response response = given()
                .filter(cookieFilter)
                .body("""
                {
                "comment" : "new expense is denied"
                }
                """)
            .when()
            .post("/api/expenses/6/deny");

        assert response.statusCode()==200;
        assert response.getBody().asString().contains("denied");
    }

    @Test
    public void denyExpenseSadTest() {
        Response response = given()
                .filter(cookieFilter)
                .body("""
                {
                "comment" : "new expense is denied"
                }
                """)
                .when()
                .post("/api/expenses/999/deny");

        assert response.statusCode()==404;
    }

    @Test
    public void denyExpenseNoAuthTest() {
        Response response = given()

                .body("""
                {
                "comment" : "new expense is denied"
                }
                """)
                .when()
                .post("/api/expenses/6/deny");

        assert response.statusCode()==401;
    }

    @Test
    public void approveExpenseHappyTest() {
        Response response = given()
                .filter(cookieFilter)
                .body("""
                {
                "comment" : "new expense is approved"
                }
                """)
                .when()
                .post("/api/expenses/6/approve");

        assert response.statusCode()==200;
        assert response.getBody().asString().contains("approved");
    }

    @Test
    public void approveExpenseSadTest() {
        Response response = given()
                .filter(cookieFilter)
                .body("""
                {
                "comment" : "new expense is approved"
                }
                """)
                .when()
                .post("/api/expenses/999/approve");

        assert response.statusCode()==404;
    }

    @Test
    public void approveExpenseNoAuthTest() {
        Response response = given()

                .body("""
                {
                "comment" : "new expense is approved"
                }
                """)
                .when()
                .post("/api/expenses/6/approve");

        assert response.statusCode()==401;
    }

}
