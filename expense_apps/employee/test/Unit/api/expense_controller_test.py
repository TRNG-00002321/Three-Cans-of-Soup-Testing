import importlib

import pytest
from unittest.mock import MagicMock
from flask import Flask

from api import expense_controller, auth
from service import ExpenseService
from repository import Expense, User

import importlib
from pytest_mock import mocker
from flask import Flask

from api import expense_controller
from api import auth
from repository.user_model import User
from repository.approval_model import Approval
from repository.expense_model import Expense

#switch to requests

import allure

@allure.suite("Unit Tests")
@allure.tag("Unit", "Sprint-2")
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
    @allure.title("Submit expense valid amount success")
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

    @allure.title("Submit expense no data failure")
    def test_submit_expense_no_data_failure(self, client):
        payload = {}
        response = client.post("/api/expenses", json=payload)

        assert response.status_code == 400

        data = response.get_json()
        assert data["error"] == "JSON data required"

    @allure.title("Submit expense no description failure")
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

    @allure.title("Submit expense no amount failure")
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
    @allure.title("Submit expense invalid amount failure")
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

    @allure.title("Submit expense service failure")
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

    @allure.title("Submit expense service data failure")
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

    @pytest.mark.parametrize("status", [
        "pending",
        "approved",
        "denied"
    ])
    @allure.title("Get expenses valid status")
    def test_get_expenses_valid_status(self, mock_client, mock_app, monkeypatch, status):
        mock_user = User(1, "test_user", "test_pass", "Employee")
        mock_expenses = [
            (
                Expense(id=1, user_id=1, amount=150.50, description="Office supplies", date="2024-01-15"),
                Approval(id=1, expense_id=1, status=status, reviewer=2, comment="Looks good", review_date="2024-01-16")
            ),
            (
                Expense(id=2, user_id=1, amount=250.50, description="Party supplies", date="2024-02-15"),
                Approval(id=2, expense_id=2, status=status, reviewer=2, comment="Approved for team event", review_date="2024-02-16")
            )
        ]
        
        monkeypatch.setattr(
            expense_controller,
            "get_current_user",
            lambda: mock_user
        )
        
        mock_service = MagicMock()
        mock_service.get_expense_history.return_value = mock_expenses
        mock_app.expense_service = mock_service
        
        response = mock_client.get(f"/api/expenses?status={status}")
        data_json = response.get_json()
        
        mock_service.get_expense_history.assert_called_once_with(
            user_id=1,
            status_filter=status
        )
        
        assert len(data_json['expenses']) == data_json['count']
        assert data_json['count'] == 2
        
        for i, expense_json in enumerate(data_json['expenses']):
            expected_expense, expected_approval = mock_expenses[i]
            
            assert expense_json['id'] == expected_expense.id
            assert expense_json['amount'] == expected_expense.amount
            assert expense_json['description'] == expected_expense.description
            assert expense_json['date'] == expected_expense.date
            assert expense_json['status'] == expected_approval.status
            assert expense_json['comment'] == expected_approval.comment
            assert expense_json['review_date'] == expected_approval.review_date
            
    @allure.title("Get expenses no status")
    def test_get_expenses_no_status(self, mock_client, mock_app, monkeypatch):
        mock_user = User(1, "test_user", "test_pass", "Employee")
        mock_expenses = [
            (
                Expense(id=1, user_id=1, amount=150.50, description="Office supplies", date="2024-01-15"),
                Approval(id=1, expense_id=1, status="pending", reviewer=2, comment="Looks good", review_date="2024-01-16")
            ),
            (
                Expense(id=2, user_id=1, amount=250.50, description="Party supplies", date="2024-02-15"),
                Approval(id=2, expense_id=2, status="pending", reviewer=2, comment="Approved for team event", review_date="2024-02-16")
            )
        ]
        
        monkeypatch.setattr(
            expense_controller,
            "get_current_user",
            lambda: mock_user
        )
        
        mock_service = MagicMock()
        mock_service.get_expense_history.return_value = mock_expenses
        mock_app.expense_service = mock_service
        
        response = mock_client.get(f"/api/expenses")
        data_json = response.get_json()
        
        mock_service.get_expense_history.assert_called_once_with(
            user_id=1,
            status_filter=None
        )
        
        assert len(data_json['expenses']) == data_json['count']
        assert data_json['count'] == 2
        
        for i, expense_json in enumerate(data_json['expenses']):
            expected_expense, expected_approval = mock_expenses[i]
            
            assert expense_json['id'] == expected_expense.id
            assert expense_json['amount'] == expected_expense.amount
            assert expense_json['description'] == expected_expense.description
            assert expense_json['date'] == expected_expense.date
            assert expense_json['status'] == expected_approval.status
            assert expense_json['comment'] == expected_approval.comment
            assert expense_json['review_date'] == expected_approval.review_date

    @allure.title("Get expenses service exception")
    def test_get_expenses_service_exception(self, mock_client, mock_app, monkeypatch):
        mock_user = User(1, "test_user", "test_pass", "Employee")
        
        monkeypatch.setattr(
            expense_controller,
            "get_current_user",
            lambda: mock_user
        )
        
        mock_service = MagicMock()
        mock_service.get_expense_history.side_effect = Exception("Database connection error")
        mock_app.expense_service = mock_service
        
        response = mock_client.get("/api/expenses?status=approved")
        
        mock_service.get_expense_history.assert_called_once_with(
            user_id=1,
            status_filter="approved"
        )