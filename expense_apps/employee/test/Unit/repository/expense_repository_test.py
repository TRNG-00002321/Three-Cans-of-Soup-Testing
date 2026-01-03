import pytest
from unittest.mock import MagicMock

from repository import Expense
from repository.expense_repository import ExpenseRepository

import allure

@allure.epic("Employee App")
@allure.feature("Expense Management")
@allure.suite("Unit Tests")
@allure.tag("Unit", "Sprint-2")
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

    @allure.story("Submit Expenses")
    @allure.title("Create expense success")
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
        
    @allure.story("Edit Pending Expenses")
    @allure.title("Update valid expense success")
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
    
    @allure.story("Edit Pending Expenses")
    @allure.title("Update None expense raises error")
    def test_update_none_expense_raisesError(self, expense_repository, conn):

        with pytest.raises(Exception):
            expense_repository.update(None)

        assert conn.commit.call_count == 0
        
    @allure.story("Delete Pending Expenses")
    @allure.title("Delete valid ID success")
    def test_delete_valid_id_returns_true(self, expense_repository, conn, cursor):
        cursor.rowcount = 1

        deleted = expense_repository.delete(0)

        assert conn.execute.call_count == 2
        conn.commit.assert_called_once()
        assert deleted
    
    @allure.story("Delete Pending Expenses")
    @allure.title("Delete invalid ID returns False")
    def test_delete_invalid_id_returns_true(self, expense_repository, conn, cursor):
        cursor.rowcount = 0

        deleted = expense_repository.delete(0)

        assert conn.execute.call_count == 2
        conn.commit.assert_called_once()
        assert not deleted

