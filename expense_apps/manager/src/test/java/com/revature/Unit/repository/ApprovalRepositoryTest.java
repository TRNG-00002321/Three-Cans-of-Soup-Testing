package com.revature.Unit.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.repository.ApprovalRepository;
import com.revature.repository.DatabaseConnection;

@Epic("Manager App")
@Feature("Expense Management")
@Tag("Unit")
@Tag("Sprint-2")
@ExtendWith(MockitoExtension.class)
public class ApprovalRepositoryTest {

    @Mock
    private DatabaseConnection databaseConnection;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @InjectMocks
    private ApprovalRepository approvalRepository;

    @BeforeEach
    void setUp() throws SQLException {
        approvalRepository = new ApprovalRepository(databaseConnection);
        when(databaseConnection.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify that update fails gracefully when no rows are affected")
    @Severity(SeverityLevel.NORMAL)
    public void updateApprovalStatus_NoUpdate_ReturnsFalse() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        boolean result = approvalRepository.updateApprovalStatus(999, "denied", 1, "No");
        assertFalse(result);

        verify(preparedStatement).setString(1, "denied");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setString(3, "No");
        verify(preparedStatement).setString(eq(4), anyString());
        verify(preparedStatement).setInt(5, 999);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify handling of SQL exceptions during approval update")
    @Severity(SeverityLevel.CRITICAL)
    void updateApprovalStatus_SQLException_ThrowsRuntimeException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB error"));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalRepository.updateApprovalStatus(123, "approved", 456, "test"));
        assertTrue(exception.getMessage().contains("Error updating approval for expense: 123"));

        verify(preparedStatement).setString(1, "approved");
        verify(preparedStatement).setInt(2, 456);
        verify(preparedStatement).setString(3, "test");
        verify(preparedStatement).setString(eq(4), anyString());
        verify(preparedStatement).setInt(5, 123);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @Story("Approve/Deny Expenses")
    @Description("Verify successful approval status update")
    @Severity(SeverityLevel.CRITICAL)
    void updateApprovalStatus_SuccessfulUpdate_ReturnsTrue() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = approvalRepository.updateApprovalStatus(123, "approved", 456, "Looks good");

        assertTrue(result);

        verify(preparedStatement).setString(1, "approved");
        verify(preparedStatement).setInt(2, 456);
        verify(preparedStatement).setString(3, "Looks good");
        verify(preparedStatement).setString(eq(4), anyString());
        verify(preparedStatement).setInt(5, 123);
        verify(preparedStatement).executeUpdate();
    }
}
