import importlib

import pytest
from unittest.mock import MagicMock
from flask import Flask

from api import expense_controller, auth
from service import ExpenseService
from repository import Expense, User

class TestExpenseController:

    @pytest.fixture()
    def expense_service(self):
        return MagicMock(spec = ExpenseService)

    @pytest.fixture()
    def expense(self):
        expense = MagicMock(spec=Expense)
        expense.id = None
        expense.user_id = 1
        expense.amount = 100
        expense.description = "Test expense controller"
        expense.date = "2025-12-15"
        return expense

    @pytest.fixture()
    def user(self):
        user = MagicMock(spec=User)
        user.id = 1
        return user

    @pytest.fixture
    def app(self, monkeypatch, expense_service, user):
        # mock require_employee_auth from auth
        monkeypatch.setattr(auth, "require_employee_auth", lambda x: x)
        monkeypatch.setattr(auth, "get_current_user", lambda: user)

        # reload imports
        expense_controller_module = importlib.reload(expense_controller)

        app = Flask(__name__)
        app.config['TESTING'] = True
        app.register_blueprint(expense_controller_module.expense_bp)
        app.expense_service = expense_service

        yield app

    @pytest.fixture()
    def client(self, app):
        return app.test_client()

    @pytest.fixture()
    def runner(self, app):
        return app.test_cli_runner()

    @pytest.mark.parametrize("amount", ["123_000", 123, "123", "12.3", 12.3])
    def test_submit_expense_valid_amount_success(self, client, expense_service, expense, amount):
        payload = {
            "amount": amount,
            "description": expense.description,
            "date": expense.date
        }
        expense.amount = amount
        expense_service.submit_expense.return_value = expense
        response = client.post("/api/expenses", json=payload)

        assert response.status_code == 201
        data = response.get_json()

        assert data["expense"]["amount"] == expense.amount
        assert data["expense"]["description"] == expense.description
        assert data["expense"]["date"] == expense.date
        assert data["expense"]["status"] == "pending"

    def test_submit_expense_no_data_failure(self, client):
        payload = {}
        response = client.post("/api/expenses", json=payload)

        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == "JSON data required"

    def test_submit_expense_no_description_failure(self, client):
        payload = {
            "amount": 100,
            "description": None,
            "date": "2025-12-15"
        }
        response = client.post("/api/expenses", json=payload)
        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == "Amount and description are required"

    def test_submit_expense_no_amount_failure(self, client):
        payload = {
            "amount": None,
            "description": "fail test",
            "date": "2025-12-15"
        }
        response = client.post("/api/expenses", json=payload)
        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == "Amount and description are required"

    @pytest.mark.parametrize("amount", ["string", "  ", "\n", "  \n", "123,00"])
    def test_submit_expense_invalid_amount_failure(self, amount, client):
        payload = {
            "amount": amount,
            "description": "fail test",
            "date": "2025-12-15"
        }
        response = client.post("/api/expenses", json=payload)
        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == "Amount must be a valid number"

    def test_submit_expense_submit_failure(self, client, expense_service):
        payload = {
            "amount": 100,
            "description": "fail test",
            "date": "2025-12-15"
        }

        expense_service.submit_expense.side_effect = Exception
        response = client.post("/api/expenses", json=payload)

        assert response.status_code == 500

        data = response.get_json()
        assert data["error"] == "Failed to submit expense"

    def test_submit_expense_submit_data_failure(self, client, expense_service):
        payload = {
            "amount": 100,
            "description": "fail test",
            "date": "2025-12-15"
        }

        expense_service.submit_expense.side_effect = ValueError
        response = client.post("/api/expenses", json=payload)

        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == ""