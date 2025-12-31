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
        print(response.json())


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


    def test_edit_expense_description_with_valid_employee_auth_and_valid_status_success(self, test_app):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': 'employee1',
            'password': 'password123'
        }).cookies.get('jwt_token')
        expense_id =1

        response = requests.put(f'{test_app}/api/expenses/{expense_id}',
                                cookies={'jwt_token': jwt_token},
                                json={
                                      'amount' : 120.99,
                                      'description': 'new description',
                                      'date' :'2024-12-03'
                                       })
        print(response.json())
        assert response.status_code == 200
        assert response.json()['expense']['amount'] == 120.99
        assert response.json()['expense']['description'] == 'new description'
        assert response.json()['expense']['date'] == '2024-12-03'



    @pytest.mark.parametrize("expense_id", [2,3])
    def test_edit_expense_description_with_valid_employee_auth_and_invalid_status(self, test_app,expense_id):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': 'employee1',
            'password': 'password123'
        }).cookies.get('jwt_token')


        response = requests.put(f'{test_app}/api/expenses/{expense_id}',
                                cookies={'jwt_token': jwt_token},
                                json={
                                      'amount' : '120.00',
                                      'description': 'new description',
                                      'date' :'2024-12-03'
                                       })
        print(response.json())
        assert response.status_code == 400
        assert 'Cannot edit expense' in response.json()['error']

    def test_delete_expense_description_with_valid_employee_auth_and_valid_status_success(self, test_app):
        jwt_token = requests.post(f'{test_app}/api/auth/login', json={
            'username': 'employee1',
            'password': 'password123'
        }).cookies.get('jwt_token')
        expense_id = 1
        response = requests.delete(f'{test_app}/api/expenses/{expense_id}',cookies={'jwt_token': jwt_token})

        assert response.status_code == 200
        assert 'Expense deleted successfully' in response.json()['message']

    @pytest.mark.parametrize("expense_id", [2,3])
    def test_delete_expense_description_with_valid_employee_auth_and_invalid_status(self, test_app,expense_id):
            jwt_token = requests.post(f'{test_app}/api/auth/login', json={
                'username': 'employee1',
                'password': 'password123'
            }).cookies.get('jwt_token')

            response = requests.delete(f'{test_app}/api/expenses/{expense_id}', cookies={'jwt_token': jwt_token})

            assert response.status_code == 400
            assert 'Cannot delete expense that has been reviewed' in response.json()['error']

