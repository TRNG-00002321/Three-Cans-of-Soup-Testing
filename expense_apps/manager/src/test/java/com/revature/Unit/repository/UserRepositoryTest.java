package com.revature.Unit.repository;

import com.revature.repository.DatabaseConnection;
import com.revature.repository.User;
import com.revature.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    private UserRepository userRepo;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseConnection mockDbConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        userRepo = new UserRepository(mockDbConnection);
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    void setUpResultSetBasicManager() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("username")).thenReturn("username");
        when(mockResultSet.getString("password")).thenReturn("password");
        when(mockResultSet.getString("role")).thenReturn("manager");
        when(mockResultSet.getInt("id")).thenReturn(1);
    }

    @Test
    void findByIdSuccessTest() throws SQLException {
        setUpResultSetBasicManager();

        User user = new User(1, "username", "password", "manager");
        Optional<User> newUser = userRepo.findById(1);
        assertTrue(newUser.isPresent());
        assertEquals(user.getId(), newUser.get().getId());
        assertEquals(user.getUsername(), newUser.get().getUsername());
        assertEquals(user.getPassword(), newUser.get().getPassword());
        assertEquals(user.getRole(), newUser.get().getRole());

        verify(mockDbConnection, times(1)).getConnection();
        verify(mockConnection).prepareStatement(argThat(arg -> arg.contains("WHERE id")));;
        verify(mockStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    void findByIdConnectionFailTest(){
        assertThrows(RuntimeException.class, () -> userRepo.findById(1));
    }

    @Test
    void findByIdNoResultTest() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        assertFalse(userRepo.findById(1).isPresent());
    }

    @Test
    void findByUsernameSuccessTest() throws SQLException {
        setUpResultSetBasicManager();

        User user = new User(1, "username", "password", "manager");
        Optional<User> newUser = userRepo.findByUsername("username");
        assertTrue(newUser.isPresent());
        assertEquals(user.getId(), newUser.get().getId());
        assertEquals(user.getUsername(), newUser.get().getUsername());
        assertEquals(user.getPassword(), newUser.get().getPassword());
        assertEquals(user.getRole(), newUser.get().getRole());

        verify(mockDbConnection, times(1)).getConnection();
        verify(mockConnection).prepareStatement(argThat(arg -> arg.contains("WHERE username")));;
        verify(mockStatement, times(1)).executeQuery();
        verify(mockResultSet, times(1)).next();
    }

    @Test
    void findByUsernameConnectionFailTest(){
        assertThrows(RuntimeException.class, () -> userRepo.findByUsername("username"));
    }

    @Test
    void findByUsernameNoResultTest() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        assertFalse(userRepo.findByUsername("username").isPresent());
    }

}