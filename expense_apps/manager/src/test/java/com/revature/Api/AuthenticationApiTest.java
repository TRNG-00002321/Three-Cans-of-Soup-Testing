package com.revature.Api;

import com.revature.api.AuthenticationMiddleware;
import com.revature.service.AuthenticationService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.restassured.RestAssured;
import io.restassured.RestAssured.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:5001/";
    }

    @Test
    void validateManagerValidManagerAuthSuccess() {

    }
    @Test
    void validateManagerInvalidAuthThrowException() {
        //maybe
    }

    @Test
    void validateUserEmployeeAuthThrowException() {

    }

    @Test
    void getAuthenticationManagerSuccess() {
        //this one is a maybe
    }
}
