import pytest
from unittest.mock import MagicMock

from expense_apps.employee.repository.expense_repository import ExpenseRepository, Expense

class TestExpenseRepository:
    """
    create
    find_by_id
    find_by_user_id
    update_
    delete
    """

    @pytest.fixture
    def mock_cursor(self):
        cursor = MagicMock()
        return cursor

    @pytest.fixture
    def mock_conn(self, mock_cursor):
        conn = MagicMock()
        conn.__enter__.return_value = conn
        conn.__exit__.return_value = None
        conn.cursor = mock_cursor
        return conn

    @pytest.fixture
    def mock_db(self, mock_conn):
        db = MagicMock()
        db.get_connection.return_value = mock_conn
        return db

    @pytest.fixture
    def expense(self):
        expense = MagicMock(spec=Expense)
        expense.user_id = 1
        expense.amount = 100
        expense.description = "Test expense repository"
        expense.date = "2025/12/16"
        return expense

    def test_create_expense_valid_expense(self, mock_db, mock_conn, mock_cursor, expense):
        repo = ExpenseRepository(mock_db)
        create = repo.create
        create(expense)
        mock_db.get_connection.assert_called_once()
        assert mock_conn.execute.call_count == 2
        mock_conn.commit.assert_called_once()


