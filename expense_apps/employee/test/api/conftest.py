import pytest
from flask import Flask
from unittest.mock import MagicMock

from api.auth_controller import auth_bp

@pytest.fixture
def app():
    app = Flask(__name__)
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
