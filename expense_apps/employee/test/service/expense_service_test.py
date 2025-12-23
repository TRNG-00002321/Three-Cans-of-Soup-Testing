import pytest
from unittest.mock import Mock

from service.expense_service import ExpenseService
from repository.expense_model import Expense
from repository.approval_model import Approval

@pytest.fixture
def expense_repo(mocker):
    return mocker.Mock()


@pytest.fixture
def approval_repo(mocker):
    return mocker.Mock()


@pytest.fixture
def service(expense_repo, approval_repo):
    return ExpenseService(expense_repo, approval_repo)

def test_get_user_expenses_with_status(service, approval_repo):
    expense = Expense(id=1, user_id=10, amount=100, description="Taxi", date="2025-01-01")
    approval = Approval(id=1, expense_id=1, status="pending")

    approval_repo.find_expenses_with_status_for_user.return_value = [
        (expense, approval)
    ]

    result = service.get_user_expenses_with_status(user_id=10)

    assert result == [(expense, approval)]
    approval_repo.find_expenses_with_status_for_user.assert_called_once_with(10)
