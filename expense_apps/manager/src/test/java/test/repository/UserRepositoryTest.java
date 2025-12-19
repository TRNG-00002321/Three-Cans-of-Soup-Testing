package com.revature.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {
    @Mock
    private UserRepository userRepo;

    @Mock
    private Connection mockConn;

    @Mock
    private PreparedStatement mockPS;

    @BeforeAll
    static void mockDatabaseConnection() {


    }

}
