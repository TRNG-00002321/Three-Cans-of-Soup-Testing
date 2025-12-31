import pytest
import requests

class TestAuthApi:

    def test_login_with_valid_credentials(self, base_url):
        login = {"username":"employee1",
                 "password":"password123"}

        response = requests.post(f"{base_url}/api/auth/login", json=login)
        data = response.json()

        assert response.status_code == 200
        assert data["message"] == "Login successful"
        assert data["user"]["username"] == "employee1"
        assert "jwt_token" in response.cookies

    def test_login_with_invalid_credentials(self, base_url):
        login = {"username": "employee1",
                 "password": "wrongPassword"}

        response = requests.post(f"{base_url}/api/auth/login", json=login)
        data = response.json()

        assert response.status_code == 401
        assert data["error"] == "Invalid credentials"

    @pytest.mark.parametrize("data", [{"username": "employee1"}, {"password": "password123"}])
    def test_login_missing_field(self, base_url, data):
        response = requests.post(f"{base_url}/api/auth/login", json=data)
        data = response.json()

        assert response.status_code == 400
        assert data["error"] == "Username and password required"


    def test_logout(self, base_url):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        response = session.post(f"{base_url}/api/auth/login", json=login)

        assert "jwt_token" in response.cookies

        response = session.post(f"{base_url}/api/auth/logout")
        data = response.json()

        assert response.status_code == 200
        assert data["message"] == "Logout successful"
        assert "jwt_token" not in response.cookies

    def test_status_logged_in(self, base_url):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        session.post(f"{base_url}/api/auth/login", json=login)
        response = session.get(f"{base_url}/api/auth/status")
        data = response.json()
        assert response.status_code == 200
        assert data["authenticated"] == True
        assert data["user"]["username"]  == "employee1"

    def test_status_not_logged_in(self, base_url):
        response = requests.get(f"{base_url}/api/auth/status")
        data = response.json()
        assert response.status_code == 200
        assert data["authenticated"] == False

    def test_status_logged_out(self, base_url):
        session = requests.Session()
        login = {"username": "employee1",
                 "password": "password123"}

        session.post(f"{base_url}/api/auth/login", json=login)
        session.post(f"{base_url}/api/auth/logout")
        response = session.get(f"{base_url}/api/auth/status")
        data = response.json()
        assert response.status_code == 200
        assert data["authenticated"] == False