
import pytest
from unittest.mock import Mock, MagicMock
from datetime import datetime, timedelta
import jwt
from service.authentication_service import AuthenticationService
from repository.user_repository import UserRepository
from repository.user_model import User

@pytest.fixture
def mock_user_repository():
    return Mock(spec=UserRepository)

@pytest.fixture
def auth_service(mock_user_repository):
    return AuthenticationService(mock_user_repository, jwt_secret_key='test-secret')

@pytest.fixture
def sample_user():
    return User(id=1, username='testuser', password='password', role='Employee')

class Test_Authentication_Service():

    def test_authenticate_user_success(self, auth_service, mock_user_repository, sample_user):
        mock_user_repository.find_by_username.return_value = sample_user
        
        user = auth_service.authenticate_user('testuser', 'password')
        
        assert user == sample_user
        mock_user_repository.find_by_username.assert_called_once_with('testuser')

    def test_authenticate_user_failure_wrong_password(self, auth_service, mock_user_repository, sample_user):
        mock_user_repository.find_by_username.return_value = sample_user
        
        user = auth_service.authenticate_user('testuser', 'wrongpassword')
        
        assert user is None
        mock_user_repository.find_by_username.assert_called_once_with('testuser')

    def test_authenticate_user_failure_user_not_found(self, auth_service, mock_user_repository):
        mock_user_repository.find_by_username.return_value = None
        
        user = auth_service.authenticate_user('nonexistent', 'password')
        
        assert user is None
        mock_user_repository.find_by_username.assert_called_once_with('nonexistent')

    def test_get_user_by_id(self, auth_service, mock_user_repository, sample_user):
        mock_user_repository.find_by_id.return_value = sample_user
        
        user = auth_service.get_user_by_id(1)
        
        assert user == sample_user
        mock_user_repository.find_by_id.assert_called_once_with(1)

    def test_generate_jwt_token(self, auth_service, sample_user):
        token = auth_service.generate_jwt_token(sample_user)
        
        assert isinstance(token, str)
        decoded = jwt.decode(token, 'test-secret', algorithms=['HS256'])
        assert decoded['user_id'] == sample_user.id
        assert decoded['username'] == sample_user.username
        assert decoded['role'] == sample_user.role

    def test_validate_jwt_token_valid(self, auth_service, sample_user):
        token = auth_service.generate_jwt_token(sample_user)
        
        payload = auth_service.validate_jwt_token(token)
        
        assert payload is not None
        assert payload['user_id'] == sample_user.id

    def test_validate_jwt_token_invalid(self, auth_service, sample_user):
        token = auth_service.generate_jwt_token("invalid.token")
        
        result = auth_service.validate_jwt_token(token)
        
        assert result is None

    def test_validate_jwt_token_expired(self, auth_service, sample_user):
        payload = {
            'user_id': sample_user.id,
            'username': sample_user.username,
            'role': sample_user.role,
            'exp': datetime.utcnow() - timedelta(hours=1), # Expired 1 hour ago
            'iat': datetime.utcnow() - timedelta(hours=2)
        }
        token = jwt.encode(payload, 'test-secret', algorithm='HS256')
        
        result = auth_service.validate_jwt_token(token)
        
        assert result is None

    def test_validate_jwt_token_invalid(self, auth_service):
        result = auth_service.validate_jwt_token("invalid.token.string")
        assert result is None

    def test_get_user_from_token_valid(self, auth_service, mock_user_repository, sample_user):
        token = auth_service.generate_jwt_token(sample_user)
        mock_user_repository.find_by_id.return_value = sample_user
        
        user = auth_service.get_user_from_token(token)
        
        assert user == sample_user
        mock_user_repository.find_by_id.assert_called_once_with(sample_user.id)

    def test_get_user_from_token_invalid(self, auth_service):
        user = auth_service.get_user_from_token("invalid.token")
        assert user is None
