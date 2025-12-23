"""
Unit tests for ExpenseRepository Class
"""
import pytest
from repository.expense_repository import ExpenseRepository, DatabaseConnection


class TestExpenseRepository():
        

    def test_delete_valid_id_returns_true(self, expense_repository, conn, cursor):
        cursor.rowcount = 1

        deleted = expense_repository.delete(0)

        assert conn.execute.call_count == 2
        conn.commit.assert_called_once()
        assert deleted
    
    def test_delete_invalid_id_returns_true(self, expense_repository, conn, cursor):
        cursor.rowcount = 0

        deleted = expense_repository.delete(0)

        assert conn.execute.call_count == 2
        conn.commit.assert_called_once()
        assert not deleted