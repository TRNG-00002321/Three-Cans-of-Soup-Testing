package com.revature.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpenseRepositoryTest {

    @Mock
    private DatabaseConnection databaseConnection;

    @InjectMocks
    private ExpenseRepository expenseRepository;

    private ResultSet getMockResultSet() throws SQLException {
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getInt("user_id")).thenReturn(2);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0);
        when(mockResultSet.getString("description")).thenReturn("Test Description");
        when(mockResultSet.getString("date")).thenReturn("2023-10-15");
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("role")).thenReturn("employee");
        when(mockResultSet.getInt("approval_id")).thenReturn(1);
        when(mockResultSet.getString("status")).thenReturn("pending");
        when(mockResultSet.getObject("reviewer")).thenReturn(null);
        when(mockResultSet.getString("comment")).thenReturn(null);
        when(mockResultSet.getString("review_date")).thenReturn(null);

        return mockResultSet;
    }

    @Test
    public void findPendingExpensesWithUsers_HasPendingExpenses_ReturnsList() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = getMockResultSet();
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findPendingExpensesWithUsers();

        // Assert
        assertEquals(1, result.size());
        Expense expense = result.get(0).getExpense();
        User user = result.get(0).getUser();
        Approval approval = result.get(0).getApproval();
        assertAll(
                () -> assertEquals(1, expense.getId()),
                () -> assertEquals((Integer) 2, expense.getUserId()),
                () -> assertEquals(100.0, expense.getAmount()),
                () -> assertEquals("Test Description", expense.getDescription()),
                () -> assertEquals("2023-10-15", expense.getDate()),
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("employee", user.getRole()),
                () -> assertEquals(1, approval.getId()),
                () -> assertEquals("pending", approval.getStatus()),
                () -> assertEquals(null, approval.getReviewer()),
                () -> assertEquals(null, approval.getComment()),
                () -> assertEquals(null, approval.getReviewDate())
        );
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void findPendingExpensesWithUsers_NoPendingExpenses_ReturnsEmptyList() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockResultSet.next()).thenReturn(false);
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findPendingExpensesWithUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();

    }

    @Test
    public void findPendingExpensesWithUsers_ThrowsRuntimeException() throws SQLException {
        // Arrange
        when(databaseConnection.getConnection()).thenThrow(SQLException.class);

        // Act
        Executable action = () -> expenseRepository.findPendingExpensesWithUsers();

        // Assert
        RuntimeException rte = assertThrows(RuntimeException.class, action);
        assertTrue(rte.getCause() instanceof SQLException);
        assertTrue(rte.getMessage().contains("Error finding pending expenses"));
        verify(databaseConnection, times(1)).getConnection();
    }

    @Test
    public void findAllExpensesWithUsers_HasPendingExpenses_ReturnsList() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = getMockResultSet();
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findAllExpensesWithUsers();

        // Assert
        assertEquals(1, result.size());
        Expense expense = result.get(0).getExpense();
        User user = result.get(0).getUser();
        Approval approval = result.get(0).getApproval();
        assertAll(
                () -> assertEquals(1, expense.getId()),
                () -> assertEquals((Integer) 2, expense.getUserId()),
                () -> assertEquals(100.0, expense.getAmount()),
                () -> assertEquals("Test Description", expense.getDescription()),
                () -> assertEquals("2023-10-15", expense.getDate()),
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("employee", user.getRole()),
                () -> assertEquals(1, approval.getId()),
                () -> assertEquals("pending", approval.getStatus()),
                () -> assertEquals(null, approval.getReviewer()),
                () -> assertEquals(null, approval.getComment()),
                () -> assertEquals(null, approval.getReviewDate())
        );
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();
    }

    @Test
    public void findAllExpensesWithUsers_NoPendingExpenses_ReturnsEmptyList() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockResultSet.next()).thenReturn(false);
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findAllExpensesWithUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();

    }

    @Test
    public void findAllExpensesWithUsers_ThrowsRuntimeException() throws SQLException {
        // Arrange
        when(databaseConnection.getConnection()).thenThrow(SQLException.class);

        // Act
        Executable action = () -> expenseRepository.findAllExpensesWithUsers();

        // Assert
        RuntimeException rte = assertThrows(RuntimeException.class, action);
        assertTrue(rte.getCause() instanceof SQLException);
        assertTrue(rte.getMessage().contains("Error finding all expenses"));
        verify(databaseConnection, times(1)).getConnection();
    }

    @Test
    public void findExpensesByUsers_ValidUserId_ReturnsList() throws SQLException {
        // Arrange
        int userId = 2;
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = getMockResultSet();
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(1, userId);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findExpensesByUser(userId);

        // Assert
        assertEquals(1, result.size());
        Expense expense = result.get(0).getExpense();
        User user = result.get(0).getUser();
        Approval approval = result.get(0).getApproval();
        assertAll(
                () -> assertEquals(1, expense.getId()),
                () -> assertEquals((Integer) userId, expense.getUserId()),
                () -> assertEquals(100.0, expense.getAmount()),
                () -> assertEquals("Test Description", expense.getDescription()),
                () -> assertEquals("2023-10-15", expense.getDate()),
                () -> assertEquals("testuser", user.getUsername()),
                () -> assertEquals("employee", user.getRole()),
                () -> assertEquals(1, approval.getId()),
                () -> assertEquals("pending", approval.getStatus()),
                () -> assertEquals(null, approval.getReviewer()),
                () -> assertEquals(null, approval.getComment()),
                () -> assertEquals(null, approval.getReviewDate())
        );
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockPreparedStatement, times(1)).setInt(1, userId);
    }

    @Test
    public void findExpensesByUsers_InvalidUserId_ReturnsEmptyList() throws SQLException {
        // Arrange
        int userId = 999;
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockResultSet.next()).thenReturn(false);
        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(1, userId);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Act
        List<ExpenseWithUser> result = expenseRepository.findExpensesByUser(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(databaseConnection, times(1)).getConnection();
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockPreparedStatement, times(1)).setInt(1, userId);

    }

    @Test
    public void findExpensesByUsers_ThrowsRuntimeException() throws SQLException {
        // Arrange
        int userId = 2;
        when(databaseConnection.getConnection()).thenThrow(SQLException.class);

        // Act
        Executable action = () -> expenseRepository.findExpensesByUser(userId);

        // Assert
        RuntimeException rte = assertThrows(RuntimeException.class, action);
        assertTrue(rte.getCause() instanceof SQLException);
        assertTrue(rte.getMessage().contains("Error finding expenses for user: " + userId));
        verify(databaseConnection, times(1)).getConnection();
    }
}
