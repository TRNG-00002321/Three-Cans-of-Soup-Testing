"""
Unit tests for ExpenseRepository Class
"""
import pytest
from repository.expense_repository import *



class TestExpenseRepository():

    def test_update_valid_expense_returns(self, expense_repository, conn):
        expense = Expense(
                id=0,
                user_id=0,
                amount=0.0,
                description='',
                date='')

        result = expense_repository.update(expense)

        assert conn.execute.call_count == 1
        conn.commit.assert_called_once()
        assert result is not None
    
    def test_update_none_expense_raisesError(self, expense_repository, conn):

        with pytest.raises(Exception):
            expense_repository.update(None)

        assert conn.commit.call_count == 0