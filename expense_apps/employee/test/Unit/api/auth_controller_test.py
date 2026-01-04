import pytest
from unittest.mock import Mock, MagicMock
from flask import Flask



import allure

@allure.suite("Unit Tests")
@allure.tag("Unit", "Sprint-2")
class Test_Auth_Controller():
    @allure.title("Login valid user")
    def test_login_valid_user(self, client, app, mock_employee):
        # Arrange
        app.auth_service.authenticate_user.return_value = mock_employee
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

    @allure.title("Login invalid user")
    def test_login_invalid_user(self, client, app):
        # Arrange
        app.auth_service.authenticate_user.return_value = None

        # Act
        response = client.post("api/auth/login", json={'username' : 'invaliduser', 'password' : 'invalidpass'})

        # Assert
        assert 401 == response.status_code
        app.auth_service.authenticate_user.assert_called_once()
        assert response.json['error'] == 'Invalid credentials'
        
    @pytest.mark.parametrize("json_data, error_message", [
        ('', 'JSON data required'),
        ({}, 'JSON data required'),
        ({'password' : 'password123'}, 'Username and password required'),
        ({'username' : 'employee1'}, 'Username and password required'),
        ({'username': '', 'password': ''}, 'Username and password required'),
        ])
    @allure.title("Login invalid body")
    def test_login_invalid_body(self, client, json_data, error_message):
        # Act
        response = client.post("api/auth/login", json=json_data)

        # Assert
        assert 400 == response.status_code
        assert error_message == response.json['error']
    
    @allure.title("Logout")
    def test_logout(self, client):
        response = client.post("api/auth/logout")
        assert 'jwt_token' in response.headers['Set-Cookie']
        print(response.headers['Set-Cookie'])
        assert 'Expires=Thu, 01 Jan 1970 00:00:00 GMT' in response.headers['Set-Cookie']

    @allure.title("Status authenticated")
    def test_status_authenicated(self, client, app, mock_employee):
        client.set_cookie('jwt_token', 'fake_token')
        app.auth_service.get_user_from_token.return_value = mock_employee

        response = client.get("api/auth/status")
        # Assert
        assert response.status_code == 200
        assert response.json['authenticated']
        assert response.json['user']['id'] == 1
        assert response.json['user']['username'] == 'employee1'
        assert response.json['user']['role'] == 'employee'
        
    @allure.title("Status not authenticated")
    def test_status_not_authenicated(self, client, app, mock_employee):

        response = client.get("api/auth/status")
        # Assert
        assert response.status_code == 200
        assert not response.json['authenticated']


    #TODO: Log Defect in Jira
    @pytest.mark.skip("Incorrect Status Code - JIRA: EMS-42 ")
    @allure.title("Status server error")
    def test_status_server_error(self, client, app):
        # Arrange
        app.auth_service.get_user_from_token.side_effect = Exception("Authentication failed")
        client.set_cookie('jwt_token', 'fake_token')

        # Act
        response = client.get("/api/auth/status")

        # Assert
        assert response.status_code == 500
        assert response.json['error'] == 'Internal server error'
        assert 'Database connection failed' in response.json['error']
