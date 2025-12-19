import pytest
from unittest.mock import Mock, MagicMock

from flask import Flask

from expense_apps.employee.api import expense_controller


class TestExpenseController():
    
    #functionname_input_expectedoutput

    @pytest.fixture
    def app(self):
        app = Flask(__name__)
        app.config['TESTING'] = True
        app.register_blueprint(expense_controller.expense_bp)
        return app

    @pytest.fixture
    def client(self, app):
        return app.test_client()

    @pytest.fixture
    def expense(self):
        expense = MagicMock()
        expense.user_id = 1
        expense.amount = 100
        expense.description = "Test expense controller"
        expense.date = "2025/12/16"
        return expense

    def test_submit_expense_valid_expense(self, expense):

        pass

    def test_submit_expense_invalid_expense(self, expense):
        pass

    def test_get_expenses_for_user(self):
        pass

    def test_get_expense_for_id(self):
        pass

    def test_update_expense_by_id(self):
        pass

    def test_delete_expense_by_id(self):
        pass
    