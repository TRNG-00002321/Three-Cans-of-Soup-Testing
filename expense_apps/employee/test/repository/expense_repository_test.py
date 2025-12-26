import pytest
from unittest.mock import MagicMock

from repository import Expense
from repository.expense_repository import ExpenseRepository

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
        expense = MagicMock(spec = Expense)
        expense.user_id = 1
        expense.amount = 100
        expense.description = "Test expense"
        expense.date = "2025/12/16"
        return expense

    def test_create_expense(self, mock_db, mock_conn, mock_cursor, expense):
        mock_cursor.lastrowid = 1
        mock_conn.execute.side_effect = [mock_cursor, None]
        repo = ExpenseRepository(mock_db)
        create = repo.create

        new_expense = create(expense)

        mock_db.get_connection.assert_called_once()
        assert mock_conn.execute.call_count == 2
        assert new_expense.id == 1
        mock_conn.commit.assert_called_once()


