"""
Unit tests for ExpenseService Class
"""
import pytest
from service.expense_service import ExpenseService
from unittest.mock import Mock

class TestExpenseService():

    def test_expense_repo_mock(self,expense_repository):
        assert expense_repository.find_by_id(0) is not None
        assert expense_repository.find_by_id(-1) is None

    @pytest.mark.parametrize(
            "status",
            [
                'approved',
                'denied'
            ],)
    def test_delete_reviewed_expense_rasies_error(self, status,
                                                  expense_service: ExpenseService, expense_repository, expense, approval):
        expense_repository.delete.side_effect = lambda expense_id: expense_id >= 0
        expense.user_id = 0
        approval.status = status
        expense_id = 0
        user_id = 0 

        with pytest.raises(ValueError) as context:
            expense_service.delete_expense(expense_id, user_id)
        
        assert 'Cannot delete expense that has been reviewed' == str(context.value)
        expense_repository.delete.assert_not_called()

    @pytest.mark.parametrize(
            "expense_id,user_id,expected",
            [
                ( 0,  0, True),
                ( 0, -1, False),
                (-1,  0, False),
                (-1, -1, False)
            ],)
    def test_delete_expense_parameterized(self, expense_id, user_id, expected,
                                          expense_service: ExpenseService, expense_repository ,expense, approval):
        expense_repository.delete.side_effect = lambda expense_id: expense_id >= 0
        expense.user_id = 0
        approval.status = 'pending'

        result = expense_service.delete_expense(expense_id, user_id)

        if expected:
            assert result
            expense_repository.delete.assert_called_once_with(expense_id)
        else:
            assert not result
            expense_repository.delete.assert_not_called()

