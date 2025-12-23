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
    expense = Expense(id=1, user_id=1, amount=100, description="Taxi", date="2025-01-01")
    approval = Approval(id=None, expense_id=1, status="pending", reviewer=None, comment=None, review_date=None)

    approval_repo.find_expenses_with_status_for_user.return_value = [
        (expense, approval)
    ]

    result = service.get_user_expenses_with_status(user_id=1)

    assert result == [(expense, approval)]
    approval_repo.find_expenses_with_status_for_user.assert_called_once_with(1)

def test_get_expense_by_id_success(service, expense_repo):
    expense = Expense(id=1, user_id=1, amount=50, description="Lunch", date="2025-01-01")

    expense_repo.find_by_id.return_value = expense

    result = service.get_expense_by_id(expense_id=1, user_id=1)

    assert result == expense
    expense_repo.find_by_id.assert_called_once_with(1)

def test_get_expense_by_id_wrong_user(service, expense_repo):
    expense = Expense(id=1, user_id=99, amount=50, description="Lunch", date="2025-01-01")

    expense_repo.find_by_id.return_value = expense

    result = service.get_expense_by_id(expense_id=1, user_id=1)

    assert result is None

def test_get_expense_by_id_not_found(service, expense_repo):
    expense_repo.find_by_id.return_value = None

    result = service.get_expense_by_id(expense_id=1, user_id=1)

    assert result is None

def test_get_expense_with_status_success(service, expense_repo, approval_repo):
    expense = Expense(id=1, user_id=1, amount=75, description="Hotel", date="2025-01-01")
    approval = Approval(id=1, expense_id=1, status="approved", reviewer=2, comment="looks good", review_date="2025-01-01")

    expense_repo.find_by_id.return_value = expense
    approval_repo.find_by_expense_id.return_value = approval

    result = service.get_expense_with_status(expense_id=1, user_id=1)

    assert result == (expense, approval)

def test_get_expense_with_status_no_expense(service, expense_repo):
    expense_repo.find_by_id.return_value = None

    result = service.get_expense_with_status(expense_id=1, user_id=1)

    assert result is None

def test_get_expense_with_status_no_approval(service, expense_repo, approval_repo):
    expense = Expense(id=1, user_id=1, amount=75, description="Hotel", date="2025-01-01")

    expense_repo.find_by_id.return_value = expense
    approval_repo.find_by_expense_id.return_value = None

    result = service.get_expense_with_status(expense_id=1, user_id=1)

    assert result is None

def test_get_expense_history_no_filter(service):
    expense = Mock()
    approval = Mock(status="pending")

    service.get_user_expenses_with_status = Mock(
        return_value=[(expense, approval)]
    )

    result = service.get_expense_history(user_id=1)

    assert result == [(expense, approval)]

def test_get_expense_history_with_status_filter(service):
    expense1 = Mock()
    approval1 = Mock(status="pending")

    expense2 = Mock()
    approval2 = Mock(status="approved")

    service.get_user_expenses_with_status = Mock(
        return_value=[
            (expense1, approval1),
            (expense2, approval2)
        ]
    )

    result = service.get_expense_history(user_id=1, status_filter="approved")

    assert result == [(expense2, approval2)]

def test_get_expense_history_invalid_filter_returns_all(service):
    expense = Mock()
    approval = Mock(status="pending")

    service.get_user_expenses_with_status = Mock(
        return_value=[(expense, approval)]
    )

    result = service.get_expense_history(user_id=1, status_filter="invalid")

    assert result == [(expense, approval)]



