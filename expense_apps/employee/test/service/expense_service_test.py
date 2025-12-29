from datetime import datetime

import pytest
from unittest.mock import MagicMock

from repository import ExpenseRepository, Expense, ApprovalRepository
from service.expense_service import ExpenseService


class TestExpenseService:

    @pytest.fixture
    def expense(self):
        expense = MagicMock(spec=Expense)
        expense.id = None
        expense.user_id = 1
        expense.amount = 100
        expense.description = "Test expense service"
        expense.date = "2025-12-16"
        return expense

    @pytest.fixture
    def expense_repository(self):
        return MagicMock(spec=ExpenseRepository)

    @pytest.fixture
    def approval_repository(self):
        return MagicMock(spec=ApprovalRepository)

    @pytest.fixture
    def expense_service(self, expense_repository, approval_repository ):
        return ExpenseService(expense_repository, approval_repository)

    def test_submit_expense_valid_expense_with_date(self, expense, expense_repository,expense_service):
        expense_repository.create.return_value =  expense
        expense_service.submit_expense(expense.user_id, expense.amount, expense.description, expense.date)

        expense_repository.create.assert_called_once()
        #get what create on expense repository_test was called with
        submitted_expense = expense_repository.create.call_args.args[0]

        assert isinstance(submitted_expense, Expense)
        assert submitted_expense == expense

    def test_submit_expense_valid_expense_without_date(self, expense, expense_repository,expense_service):
        expense.date = datetime.now().strftime('%Y-%m-%d')
        expense_repository.create.return_value =  expense

        expense_service.submit_expense(expense.user_id, expense.amount, expense.description)

        expense_repository.create.assert_called_once()
        submitted_expense = expense_repository.create.call_args.args[0]

        assert isinstance(submitted_expense, Expense)
        assert submitted_expense == expense

    def test_submit_expense_valid_invalid_amount_zero(self, expense, expense_repository,expense_service):
        expense.amount = 0

        with pytest.raises(ValueError, match="Amount must be greater than 0"):
            expense_service.submit_expense(expense.user_id, expense.amount, expense.description, expense.date)
        expense_repository.create.assert_not_called()

    def test_submit_expense_valid_invalid_amount_negative(self, expense, expense_repository,expense_service):
        expense.amount = -100

        with pytest.raises(ValueError, match="Amount must be greater than 0"):
            expense_service.submit_expense(expense.user_id, expense.amount, expense.description, expense.date)
        expense_repository.create.assert_not_called()

    @pytest.mark.parametrize("description", ["", "   ", "\n", "  \n"])
    def test_submit_expense_valid_invalid_description(self, expense, expense_repository,expense_service, description):
        expense.description = description

        with pytest.raises(ValueError, match= "Description is required"):
            expense_service.submit_expense(expense.user_id, expense.amount, expense.description, expense.date)
        expense_repository.create.assert_not_called()
