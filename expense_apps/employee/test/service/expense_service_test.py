from unittest.mock import Mock

import pytest
from repository.approval_model import Approval
from repository.expense_model import Expense
from service.expense_service import ExpenseService
from sqlite3 import OperationalError


class Test_Expense_Service():
    def test_get_user_expenses_with_status_empty_result(self):
        user_id = 999
        mock_approval_repository = Mock()
        mock_expense_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.return_value = []
        
        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)
        result = expense_service.get_user_expenses_with_status(user_id)

        assert result == []
        assert len(result) == 0
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)
        
    def test_get_user_expenses_with_status_multiple_expenses(self):
        user_id = 456
        
        expenses_with_approvals = [
            (
                Expense(1, user_id, 50.00, "Lunch", "2024-01-15"),
                Approval(None, 1, "approved", None, "OK", "2024-01-16")
            ),
            (
                Expense(2, user_id, 200.00, "Travel", "2024-01-14"),
                Approval(None, 2, "pending", None, None, None)
            ),
            (
                Expense(3, user_id, 75.00, "Supplies", "2024-01-13"),
                Approval(None, 3, "denied", None, "Not valid", "2024-01-15")
            )
        ]
        
        mock_approval_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.return_value = expenses_with_approvals
        mock_expense_repository = Mock()
        
        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)
        
     
        result = expense_service.get_user_expenses_with_status(user_id)
        

        assert len(result) == 3
        assert result[0][0].amount == 50.00
        assert result[0][1].status == "approved"
        assert result[1][1].status == "pending"
        assert result[2][1].status == "denied"
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)
        
    def test_get_user_expenses_with_status_sql_error(self):
        user_id = 789
        mock_approval_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.side_effect = OperationalError("database is locked")
        mock_expense_repository = Mock()

        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)

        with pytest.raises(OperationalError) as exc_info:
            expense_service.get_user_expenses_with_status(user_id)
        
        assert "database is locked" in str(exc_info.value)
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)