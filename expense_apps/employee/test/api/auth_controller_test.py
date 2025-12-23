import pytest
from unittest.mock import Mock, MagicMock
from flask import Flask

@pytest.fixture
def app():
    app = Flask(__name__)

    app.auth_service = MagicMock()
    from api.auth_controller import auth_bp
    app.register_blueprint(auth_bp)
    return app

@pytest.fixture
def client(app):
    return app.test_client()

@pytest.fixture
def mock_user():
    user = Mock()
    user.id = 1
    user.username = 'employee1'
    user.password = 'password123'
    user.role = 'employee'
    return user


class Test_Auth_Controller():
    def test_login_valid_user(self, client, app, mock_user):
        # Arrange
        app.auth_service.authenticate_user.return_value = mock_user
        app.auth_service.generate_jwt_token.return_value = "fake-token"

        # Act
        response = client.post("api/auth/login", json={'username' : 'employee1', 'password' : 'password123'})

       # Assert
        assert response.status_code == 200
        assert response.json['user']['username'] == 'employee1'

        assert 'jwt_token' in response.headers['Set-Cookie']
        assert 'fake-token' in response.headers['Set-Cookie']

        app.auth_service.authenticate_user.assert_called_once()
        app.auth_service.generate_jwt_token.assert_called_once()

    def test_login_invalid_user(self, client, app):
        # Arrange
        app.auth_service.authenticate_user.return_value = None

        # Act
        response = client.post("api/auth/login", json={'username' : 'invaliduser', 'password' : 'invalidpass'})

       # Assert
        assert 401 == response.status_code
        app.auth_service.authenticate_user.assert_called_once()
        assert response.json['error'] == 'Invalid credentials'

    def test_login_no_body(self, client):
        # Act
        response = client.post("api/auth/login", json='')

       # Assert
        assert response.status_code == 400
        assert response.json['error'] == 'JSON data required'
        
    @pytest.mark.parametrize("json_data, error_message", [
        ({}, 'JSON data required'),
        ({'password' : 'password123'}, 'Username and password required'),
        ({'username' : 'employee1'}, 'Username and password required'),
        ({'username': '', 'password': ''}, 'Username and password required'),
        ])
    def test_login_invalid_body(self, client, json_data, error_message):
        # Act
        response = client.post("api/auth/login", json=json_data)

       # Assert
        assert 400 == response.status_code
        assert error_message == response.json['error']
    
    def test_logout(self, client):
        response = client.post("api/auth/logout")
        assert 'jwt_token' in response.headers['Set-Cookie']
        print(response.headers['Set-Cookie'])
        assert 'Expires=Thu, 01 Jan 1970 00:00:00 GMT' in response.headers['Set-Cookie']

    def test_status(self, client, app, mock_user):
        client.set_cookie('jwt_token', 'fake_token')
        app.auth_service.get_user_from_token.return_value = mock_user

        response = client.get("api/auth/status")
        # Assert
        assert response.status_code == 200
        assert response.json['authenticated']
        assert response.json['user']['id'] == 1
        assert response.json['user']['username'] == 'employee1'
        assert response.json['user']['role'] == 'employee'


    def test_status_server_error(self, client, app):
        # Arrange: Force the service to raise an exception
        app.auth_service.get_user_from_token.side_effect = Exception("Authentication failed")
        
        # We need a cookie to get past the first 'if not token' check
        client.set_cookie('jwt_token', 'fake_token')

        # Act
        response = client.get("/api/auth/status")

        # Assert
        assert response.status_code == 500
        assert response.json['error'] == 'Internal server error'
        assert 'Database connection failed' in response.json['details']
