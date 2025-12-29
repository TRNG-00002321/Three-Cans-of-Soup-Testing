from datetime import datetime

import pytest
from unittest.mock import MagicMock

from repository import ExpenseRepository, Expense, ApprovalRepository, Approval

from service.expense_service import ExpenseService
from unittest.mock import Mock

import pytest
from repository.approval_model import Approval
from repository.expense_model import Expense
from service.expense_service import ExpenseService
from sqlite3 import OperationalError


@pytest.fixture
def expense_repo(mocker):
    return mocker.Mock()


@pytest.fixture
def approval_repo(mocker):
    return mocker.Mock()


@pytest.fixture
def service(expense_repo, approval_repo):
    return ExpenseService(expense_repo, approval_repo)


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
    
    def test_get_user_expenses_with_status(self, service, approval_repo):
        expense = Expense(id=1, user_id=10, amount=100, description="Taxi", date="2025-01-01")
        approval = Approval(id=1, expense_id=1, status="pending", reviewer=None, comment=None, review_date=None)

        approval_repo.find_expenses_with_status_for_user.return_value = [
            (expense, approval)
        ]

        result = service.get_user_expenses_with_status(user_id=10)

        assert result == [(expense, approval)]
        approval_repo.find_expenses_with_status_for_user.assert_called_once_with(10)    

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
        

    def test_expense_repo_mock(self,expense_repository):
        assert expense_repository.find_by_id(0) is not None
        assert expense_repository.find_by_id(-1) is None
    
    @pytest.mark.parametrize(
            "status",
            [
                'approved',
                'denied'
            ],
    )
    def test_update_expense_reviewed_throws_error(self, status,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = status
        expense_id = 0
        user_id = 0
        amount = 1
        description = '-'
        date = ''

        with pytest.raises(ValueError) as context:
            expense_service.update_expense(expense_id,user_id,amount,description,date)

        assert 'Cannot edit expense that has been reviewed' == str(context.value)
        expense_repository.update.assert_not_called()
    
    @pytest.mark.parametrize(
            "amount,description,date,expected_error",
            [
                ( 0, '/', '', 'Amount must be greater than 0'),
                (-1, '/', '', 'Amount must be greater than 0'),
                ( 0,  '', '', 'Amount must be greater than 0'),
                ( 1,  '', '', 'Description is required'),
                ( 1, ' ', '', 'Description is required'),
                ( 1,'\n', '', 'Description is required'),
            ],
    )
    def test_update_expense_bad_value_throws_error(self, amount, description, date, expected_error,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = 'pending'
        expense_id = 0
        user_id = 0

        with pytest.raises(ValueError) as context:
            expense_service.update_expense(expense_id,user_id,amount,description,date)

        assert expected_error == str(context.value)
        expense_repository.update.assert_not_called()
        
    @pytest.mark.parametrize(
            "status",
            [
                'approved',
                'denied'
            ],
    )
    def test_delete_reviewed_expense_rasies_error(self, status,
                                                  expense_service: ExpenseService, expense_repository, expense, approval):
        expense_repository.delete.side_effect = lambda expense_id: expense_id >= 0
        expense.user_id = 0
        approval.status = status
        expense_id = 0
        user_id = 0 

        with pytest.raises(ValueError) as context:
            expense_service.delete_expense(expense_id, user_id)
        
        assert 'Cannot delete expense that has been reviewed' == str(context.value)
        expense_repository.delete.assert_not_called()

    @pytest.mark.parametrize(
            "expense_id,user_id,expected",
            [
                ( 0,  0, True),
                ( 0, -1, False),
                (-1,  0, False),
                (-1, -1, False),
            ],
    )
    def test_update_expense_parameterized_ids(self, expense_id, user_id, expected,
                                          expense_service:ExpenseService, expense_repository, expense, approval):
        expense_repository.update.side_effect = lambda expense: expense
        expense.user_id = 0
        approval.status = 'pending'
        amount = 1
        description = '-'
        date = ''

        result = expense_service.update_expense(expense_id,user_id,amount,description,date)

        if expected:
            assert result is not None
            expense_repository.update.assert_called_once_with(expense)
        else:
            assert result is None
            expense_repository.update.assert_not_called()
            
    @pytest.mark.parametrize(
            "expense_id,user_id,expected",
            [
                ( 0,  0, True),
                ( 0, -1, False),
                (-1,  0, False),
                (-1, -1, False),
            ],
    )
    def test_delete_expense_parameterized(self, expense_id, user_id, expected,
                                          expense_service: ExpenseService, expense_repository ,expense, approval):
        expense_repository.delete.side_effect = lambda expense_id: expense_id >= 0
        expense.user_id = 0
        approval.status = 'pending'

        result = expense_service.delete_expense(expense_id, user_id)

        if expected:
            assert result
            expense_repository.delete.assert_called_once_with(expense_id)
        else:
            assert not result
            expense_repository.delete.assert_not_called()

    def test_get_user_expenses_with_status_empty_result(self):
        user_id = 999
        mock_approval_repository = Mock()
        mock_expense_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.return_value = []
        
        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)
        result = expense_service.get_user_expenses_with_status(user_id)

        assert result == []
        assert len(result) == 0
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)
        
    def test_get_user_expenses_with_status_multiple_expenses(self):
        user_id = 456
        
        expenses_with_approvals = [
            (
                Expense(1, user_id, 50.00, "Lunch", "2024-01-15"),
                Approval(None, 1, "approved", None, "OK", "2024-01-16")
            ),
            (
                Expense(2, user_id, 200.00, "Travel", "2024-01-14"),
                Approval(None, 2, "pending", None, None, None)
            ),
            (
                Expense(3, user_id, 75.00, "Supplies", "2024-01-13"),
                Approval(None, 3, "denied", None, "Not valid", "2024-01-15")
            )
        ]
        
        mock_approval_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.return_value = expenses_with_approvals
        mock_expense_repository = Mock()
        
        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)
        
     
        result = expense_service.get_user_expenses_with_status(user_id)
        

        assert len(result) == 3
        assert result[0][0].amount == 50.00
        assert result[0][1].status == "approved"
        assert result[1][1].status == "pending"
        assert result[2][1].status == "denied"
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)
        
    def test_get_user_expenses_with_status_sql_error(self):
        user_id = 789
        mock_approval_repository = Mock()
        mock_approval_repository.find_expenses_with_status_for_user.side_effect = OperationalError("database is locked")
        mock_expense_repository = Mock()

        expense_service = ExpenseService(mock_expense_repository, mock_approval_repository)

        with pytest.raises(OperationalError) as exc_info:
            expense_service.get_user_expenses_with_status(user_id)
        
        assert "database is locked" in str(exc_info.value)
        mock_approval_repository.find_expenses_with_status_for_user.assert_called_once_with(user_id)