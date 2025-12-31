from unittest.mock import patch

import pytest
import requests

class TestAuthApi:

    def test_login_with_valid_credentials(self, test_app):
        login = {"username":"employee1",
                 "password":"password123"}

        response = requests.post(f"{test_app}/api/auth/login", json=login)
        data = response.json()

        assert response.status_code == 200
        assert data["message"] == "Login successful"
        assert data["user"]["username"] == "employee1"
        assert "jwt_token" in response.cookies

    def test_login_with_invalid_credentials(self, test_app):
        login = {"username": "employee1",
                 "password": "wrongPassword"}

        response = requests.post(f"{test_app}/api/auth/login", json=login)
        data = response.json()

        assert response.status_code == 401
        assert data["error"] == "Invalid credentials"
        
    @pytest.mark.skip(reason="Incorrect status code, ticket EMS-60")
    def test_login_with_manager_credentials(self, test_app):
        login = {"username": "manager1",
                 "password": "password123"}

        response = requests.post(f"{test_app}/api/auth/login", json=login)
        data = response.json()

        assert response.status_code == 401
        assert data["error"] == "Invalid credentials"

    @pytest.mark.parametrize("data", [{"username": "employee1"}, {"password": "password123"}])
    def test_login_missing_field(self, test_app, data):
        response = requests.post(f"{test_app}/api/auth/login", json=data)
        data = response.json()

        assert response.status_code == 400
        assert data["error"] == "Username and password required"

    def test_login_missing_json(self, test_app):
        response = requests.post(f"{test_app}/api/auth/login", json={})
        data = response.json()

        assert response.status_code == 400
        assert data["error"] == "JSON data required"

    def test_login_service_error(self, test_app):
        login = {"username": "employee1", "password": "password123"}
        with patch("service.authentication_service.AuthenticationService.authenticate_user",
                   side_effect=Exception("Service failed")):
            response = requests.post(f"{test_app}/api/auth/login", json=login)
        assert response.status_code == 500
        assert response.json()["error"] == "Login failed"

    def test_logout(self, test_app):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        response = session.post(f"{test_app}/api/auth/login", json=login)

        assert "jwt_token" in response.cookies

        response = session.post(f"{test_app}/api/auth/logout")
        data = response.json()

        assert response.status_code == 200
        assert data["message"] == "Logout successful"
        assert "jwt_token" not in response.cookies

    def test_status_logged_in(self, test_app):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        session.post(f"{test_app}/api/auth/login", json=login)
        response = session.get(f"{test_app}/api/auth/status")
        data = response.json()
        assert response.status_code == 200
        assert data["authenticated"] == True
        assert data["user"]["username"]  == "employee1"

    @pytest.mark.skip(reason="Incorrect status code, ticket EMS-58")
    def test_status_not_logged_in(self, test_app):
        response = requests.get(f"{test_app}/api/auth/status")
        data = response.json()
        assert response.status_code == 401
        assert data["authenticated"] == False
        
    @pytest.mark.skip(reason="Incorrect status code, ticket EMS-58")
    def test_status_logged_out(self, test_app):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        session.post(f"{test_app}/api/auth/login", json=login)
        session.post(f"{test_app}/api/auth/logout")
        response = session.get(f"{test_app}/api/auth/status")
        data = response.json()
        assert response.status_code == 401
        assert data["authenticated"] == False

    @pytest.mark.skip(reason="Incorrect status code, ticket EMS-59")
    def test_status_service_error(self, test_app):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        response = session.post(f"{test_app}/api/auth/login", json=login)
        assert "jwt_token" in response.cookies

        with patch("service.authentication_service.AuthenticationService.get_user_from_token",
                   side_effect=Exception("Service failed")):

            response = session.get(f"{test_app}/api/auth/status")
            data = response.json()
            assert response.status_code == 500
            assert data["authenticated"] == False
