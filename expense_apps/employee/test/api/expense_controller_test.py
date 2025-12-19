import pytest
from pytest_mock import mocker
from flask import Flask

from repository.approval_model import Approval
from repository.expense_model import Expense

class Test_Expense_Controller():
    @pytest.mark.parametrize("status", [
    "pending",
    "approved",
    "denied",
    ])
    def test_get_expenses_valid_status(self, status, mocker):  # noqa: F811
        mock_user = mocker.Mock()
        mock_user.id = 1
        mock_expense_service = mocker.Mock()
        mock_expenses = [
            (
                Expense(id=1, user_id=1, amount=150.50, description="Office supplies", date="2024-01-15"),
                Approval(id=1, expense_id=1, status=status, reviewer=2, comment="Looks good", review_date="2024-01-16")
            ),
            (
                Expense(id=2, user_id=1, amount=250.50, description="Party supplies", date="2024-02-15"),
                Approval(id=2, expense_id=2, status=status, reviewer=2, comment="Approved for team event", review_date="2024-02-16")
            )
        ]
        
        mock_get_current_user = mocker.patch('api.expense_controller.get_current_user', return_value=mock_user)
        mock_get_expense_service = mocker.patch('api.expense_controller.get_expense_service', return_value=mock_expense_service)
        mock_expense_service.get_expense_history.return_value = mock_expenses
        
        from api.expense_controller import expense_bp
        app = Flask(__name__)
        app.register_blueprint(expense_bp)
        app.config['TESTING'] = True
        
        with app.test_client() as client:
            response = client.get(f"/api/expenses?status={status}")
            data_json = response.get_json()
        
            # Verify calls
            mock_get_current_user.assert_called_once()
            mock_get_expense_service.assert_called_once()
            mock_expense_service.get_expense_history.assert_called_once_with(
                user_id=1,
                status_filter=status
            )
            
            assert len(data_json['expenses']) == data_json['count']
            assert data_json['count'] == 2
            
            for i, expense_json in enumerate(data_json['expenses']):
                expected_expense, expected_approval = mock_expenses[i]
                
                assert expense_json['id'] == expected_expense.id
                assert expense_json['amount'] == expected_expense.amount
                assert expense_json['description'] == expected_expense.description
                assert expense_json['date'] == expected_expense.date
                assert expense_json['status'] == expected_approval.status
                assert expense_json['comment'] == expected_approval.comment
                assert expense_json['review_date'] == expected_approval.review_date
            
    # def test_get_expenses_invalid_status():
        
    # def test_get_expenses_valid_userId():
        
    # def test_get_expenses_invalid_userId():
    
