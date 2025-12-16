import pytest
from repository.database import DatabaseConnection
from repository.user_model import User
from repository.user_repository import UserRepository

@pytest.fixture
def repo():
    return UserRepository(DatabaseConnection())

@pytest.fixture
def positive_user():
    return User(1, "employee1", "password123", "Employee")


def test_find_by_username_positive(repo, positive_user):
    assert repo.find_by_username("employee1") == positive_user

def test_find_by_username_negative(repo):
    assert repo.find_by_username("does not exist") is None

def test_find_by_id_positive(repo, positive_user):
    assert repo.find_by_id(1) == positive_user


def test_find_by_id_negative(repo):
    assert repo.find_by_id(3) is None

def test_find_by_manager_id_negative(repo):
    with pytest.raises(ValueError):
        repo.find_by_id(2)