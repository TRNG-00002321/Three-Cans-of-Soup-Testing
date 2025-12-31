import pytest
import requests


class TestExpenseController:
    
    @pytest.mark.parametrize("status", [
        "pending",
        "approved",
        "denied"
    ])
    def test_get_expenses_with_valid_employee_auth_and_valid_status_success(self, test_app, status):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': 'employee1',
            'password': 'password123'
        }).cookies.get('jwt_token')
    
        response = requests.get(f'{test_app}/api/expenses?status={status}',cookies={'jwt_token': jwt_token})
        assert response.status_code == 200
        assert response.json() is not None

    @pytest.mark.parametrize("username,password,expected_count", [
        ("employee1", "password123", 3),
        ("employee2", "password456", 2)
    ])
    def test_get_expenses_with_valid_employee_auth_and_valid_status_returns_own_expeses_success(self, test_app, username, password, expected_count):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': username,
            'password': password    
        }).cookies.get('jwt_token')
    
        response = requests.get(f'{test_app}/api/expenses',cookies={'jwt_token': jwt_token})
        assert response.status_code == 200
        assert response.json() is not None
        assert response.json()['count'] == expected_count

    def test_get_expenses_with_invalid_auth_and_valid_status_fail(self, test_app):
        jwt_token = "invalid_token"
    
        response = requests.get(f'{test_app}/api/expenses',cookies={'jwt_token': jwt_token})
        assert response.status_code == 403
        assert response.json() is not None
        
    def test_get_expenses_with_valid_manager_auth_and_valid_status_fail(self, test_app):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': 'manager1',
            'password': 'password123'
        }).cookies.get('jwt_token')
        
        response = requests.get(f'{test_app}/api/expenses',cookies={'jwt_token': jwt_token})
        assert response.status_code == 401
        assert response.json() is not None