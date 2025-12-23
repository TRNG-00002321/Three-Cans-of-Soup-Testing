from unittest.mock import MagicMock
import pytest


class Test_Expense_Controller():
    
    def test_find_expenses_with_status_for_user(self, approval_repository, mock_db_connection):
        _, mock_context = mock_db_connection
        mock_cursor = MagicMock()
        mock_context.execute.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = [
            {
                'id': 5,
                'amount': 75.25,
                'description': 'Taxi fare',
                'date': '2024-01-18',
                'status': 'denied',
                'comment': 'Test Comment 1',
                'review_date': '2024-01-19'
            },
            {
                'id': 15,
                'amount': 100.25,
                'description': 'Office supplies',
                'date': '2024-01-18',
                'status': 'approved',
                'comment': 'Test Commnet 2',
                'review_date': '2025-04-29'
            }
        ]
        
        results = approval_repository.find_expenses_with_status_for_user(2)
        assert len(results) == 2
        expense, approval = results[0]
        
        mock_context.execute.assert_called_once()
        mock_cursor.fetchall.assert_called_once()
        
        assert expense.id == 5
        assert expense.amount == 75.25
        assert expense.description == 'Taxi fare'
        assert expense.date == '2024-01-18'
        assert approval.status == 'denied'
        assert approval.comment == 'Test Comment 1'
        assert approval.review_date == '2024-01-19'
        
    def test_find_expenses_with_status_empty_result(self, approval_repository, mock_db_connection):
        _, mock_context = mock_db_connection
        mock_cursor = MagicMock()
        mock_context.execute.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = []
        
        results = approval_repository.find_expenses_with_status_for_user(2)
        assert len(results) == 0
        
        mock_context.execute.assert_called_once()
        mock_cursor.fetchall.assert_called_once()
    
    def test_find_expenses_with_status_connection_fails(self, approval_repository, mock_db_connection):
        mock_conn, _ = mock_db_connection
        mock_conn.get_connection.side_effect = Exception("Database connection failed")
        
        with pytest.raises(Exception, match="Database connection failed"):
            approval_repository.find_expenses_with_status_for_user(2)
        
        mock_conn.get_connection.assert_called_once()
        
