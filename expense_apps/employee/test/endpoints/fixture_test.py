import pytest


def test_user_repository_not_empty(user_repository):
    employee = 'employee1'

    result = user_repository.find_by_username(employee)

    assert result