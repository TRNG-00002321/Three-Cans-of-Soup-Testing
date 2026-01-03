import pytest
from unittest.mock import Mock
from service.expense_service import ExpenseService, Expense,Approval,ExpenseRepository,ApprovalRepository

@pytest.fixture()
def expense():
    return Mock(spec=Expense)

@pytest.fixture()
def approval():
    return Mock(spec=Approval)

@pytest.fixture()
def expense_repository():
    return Mock(spec=ExpenseRepository)

@pytest.fixture()
def approval_repository():
    return Mock(spec=ApprovalRepository)

@pytest.fixture(autouse=True)
def expense_repository_setup(expense_repository, expense):
    def side_effect(expense_id):
        if expense_id >= 0:
            return expense
        return None
    expense_repository.find_by_id.side_effect = side_effect

@pytest.fixture(autouse=True)
def approval_repository_setup(approval_repository, approval):
    def side_effect(approval_id):
        if approval_id >= 0:
            return approval
        return None
    approval_repository.find_by_expense_id.side_effect = side_effect

@pytest.fixture()
def expense_service(expense_repository, approval_repository):
    return ExpenseService(expense_repository, approval_repository)