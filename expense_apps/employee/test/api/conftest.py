import pytest
import importlib
from unittest.mock import MagicMock
import pytest
from pytest_mock import mocker
from flask import Flask

from api import expense_controller
from api import auth
from repository.user_model import User
from repository.approval_model import Approval
from repository.expense_model import Expense
from api.auth_controller import auth_bp
@pytest.fixture
def app(monkeypatch):
    def identity_decorator(fn):
        return fn

    # mock require_employee_auth from auth
    monkeypatch.setattr(auth, "require_employee_auth", identity_decorator)

    # reload imports
    expense_controller_module = importlib.reload(expense_controller)

    app = Flask(__name__)
    app.testing = True
    app.register_blueprint(expense_controller_module.expense_bp)

    app.auth_service = MagicMock()
    app.register_blueprint(auth_bp)

    return app

@pytest.fixture
def client(app):
    return app.test_client()

@pytest.fixture
def mock_employee():
    user = MagicMock()
    user.id = 1
    user.username = "employee1"
    user.role = "employee"
    return user

@pytest.fixture
def mock_manager():
    user = MagicMock()
    user.id = 2
    user.useranme = "manager1" 
    user.role = "manager"
    return user
