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
            ],
    )
    def test_update_expense_reviewed_throws_error(self, status,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = status
        expense_id = 0
        user_id = 0
        amount = 1
        description = '-'
        date = ''

        with pytest.raises(ValueError) as context:
            expense_service.update_expense(expense_id,user_id,amount,description,date)

        assert 'Cannot edit expense that has been reviewed' == str(context.value)
        expense_repository.update.assert_not_called()
    
    @pytest.mark.parametrize(
            "amount,description,date,expected_error",
            [
                ( 0, '/', '', 'Amount must be greater than 0'),
                (-1, '/', '', 'Amount must be greater than 0'),
                ( 0,  '', '', 'Amount must be greater than 0'),
                ( 1,  '', '', 'Description is required'),
                ( 1, ' ', '', 'Description is required'),
                ( 1,'\n', '', 'Description is required'),
            ],
    )
    def test_update_expense_bad_value_throws_error(self, amount, description, date, expected_error,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = 'pending'
        expense_id = 0
        user_id = 0

        with pytest.raises(ValueError) as context:
            expense_service.update_expense(expense_id,user_id,amount,description,date)

        assert expected_error == str(context.value)
        expense_repository.update.assert_not_called()
        
    @pytest.mark.parametrize(
            "expense_id,user_id,expected",
            [
                ( 0,  0, True),
                ( 0, -1, False),
                (-1,  0, False),
                (-1, -1, False),
            ],
    )
    def test_update_expense_parameterized_ids(self, expense_id, user_id, expected,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = 'pending'
        amount = 1
        description = '-'
        date = ''

        result = expense_service.update_expense(expense_id,user_id,amount,description,date)

        if expected:
            assert result is not None
            expense_repository.update.assert_called_once_with(expense)
        else:
            assert result is None
            expense_repository.update.assert_not_called()



