import pytest
from unittest.mock import Mock, MagicMock
from flask import Flask, jsonify
from api.auth import require_employee_auth

def test_require_employee_auth_no_token(app, client):
    @app.route('/test-auth')
    @require_employee_auth
    def protected_view():
        return "success"
        
    response = client.get('/test-auth')
    
    assert response.status_code == 401
    assert response.json == {'error': 'Authentication required'}

def test_require_employee_auth_invalid_token(app, client):
    @app.route('/test-auth-invalid')
    @require_employee_auth
    def protected_view_invalid():
        return "success"
    
    app.auth_service.get_user_from_token.return_value = None
    
    client.set_cookie('jwt_token', 'invalid_token')

    
    response = client.get('/test-auth-invalid')

    assert response.status_code == 403
    assert response.json == {'error': 'Access denied'}

    
def test_require_employee_auth_wrong_role(app, client):
    @app.route('/test-auth-role')
    @require_employee_auth
    def protected_view_role():
        return "success"
        
    # Mock user with wrong role
    mock_user = Mock()
    mock_user.role = 'Manager'
    app.auth_service.get_user_from_token.return_value = mock_user
    
    client.set_cookie('jwt_token', 'valid_token_wrong_role')
    
    response = client.get('/test-auth-role')
    
    assert response.status_code == 403
    assert response.json == {'error': 'Access denied'}

def test_require_employee_auth_success(app, client):
    @app.route('/test-auth-success')
    @require_employee_auth
    def protected_view_success():
        return "success"
        
    # Mock user with correct role
    mock_user = Mock()
    mock_user.role = 'Employee'
    app.auth_service.get_user_from_token.return_value = mock_user
    
    client.set_cookie('jwt_token', 'valid_token')
    
    response = client.get('/test-auth-success')
    
    assert response.status_code == 200
    assert response.data.decode() == "success"