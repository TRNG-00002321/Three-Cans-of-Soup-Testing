import json
import os
import sys
import threading
import time

import pytest
from werkzeug.serving import make_server

# Add parent directories to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from repository.database import DatabaseConnection

TEMP_DATABASE = 'tmp_database.db'

@pytest.fixture
def app(user_repository, expense_repository, approval_repository):
    app = 0
    yield app

# Test database path - SEPARATE from production
TEST_DB_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 
                            'Api', 'test_expense_manager.db')
SEED_SQL_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 
                             'Api', 'seed_data_20241229.sql')


@pytest.fixture(scope='module')
def test_app():
    """
    Create Flask test application with real database and live server.
    """
    # Initialize test database
    db_conn = DatabaseConnection(TEST_DB_PATH)
    #db_conn.initialize_database()
    
    # Load seed data
    with open(SEED_SQL_PATH, 'r') as f:
        seed_sql = f.read()
    
    with db_conn.get_connection() as conn:
        conn.executescript(seed_sql)
        conn.commit()
    
    # Set environment variable for app to use test database
    os.environ['DATABASE_PATH'] = TEST_DB_PATH
    
    # Import and create app after setting env var
    from main import create_app
    app = create_app()
    app.config['TESTING'] = True
    
    # Start server in background thread
    server = make_server('127.0.0.1', 5000, app, threaded=True)
    thread = threading.Thread(target=server.serve_forever)
    thread.daemon = True
    thread.start()
    
    # Wait for server to be ready
    time.sleep(0.5)
    
    yield 'http://127.0.0.1:5000'
    
    # Cleanup
    server.shutdown()


# @pytest.fixture
# def client(test_app):
#     """Create Flask test client."""
#     return test_app.test_client()


# @pytest.fixture
# def authenticated_client(client):
#     """Create authenticated client session."""
#     # Login as employee1
#     response = client.post('/api/auth/login', 
#                           data=json.dumps({
#                               'username': 'employee1',
#                               'password': 'password123'
#                           }),
#                           content_type='application/json')
    
#     # The test client automatically handles cookies
#     return client

# IAN FIXTURES BROKEN IMPORTS!

# import pytest
# from flask import Flask
# from unittest.mock import MagicMock

# from api.auth_controller import auth_bp
# import importlib
# from pytest_mock import mocker

# from api import expense_controller
# from api import auth
# from repository.user_model import User
# from repository.approval_model import Approval
# from repository.expense_model import Expense

# @pytest.fixture
# def app():
#     app = Flask(__name__)
#     app.auth_service = MagicMock()
#     app.register_blueprint(auth_bp)
#     return app

# @pytest.fixture
# def client(app):
#     return app.test_client()

# @pytest.fixture
# def mock_employee():
#     user = MagicMock()
#     user.id = 1
#     user.username = "employee1"
#     user.role = "employee"
#     return user

# @pytest.fixture
# def mock_manager():
#     user = MagicMock()
#     user.id = 2
#     user.useranme = "manager1" 
#     user.role = "manager"
#     return user

# @pytest.fixture
# def mock_app(monkeypatch):
#     def identity_decorator(fn):
#         return fn

#     # mock require_employee_auth from auth
#     monkeypatch.setattr(auth, "require_employee_auth", identity_decorator)

#     # reload imports
#     expense_controller_module = importlib.reload(expense_controller)

#     app = Flask(__name__)
#     app.testing = True
#     app.register_blueprint(expense_controller_module.expense_bp)

#     return app

# @pytest.fixture
# def mock_client(mock_app):
#     return mock_app.test_client()