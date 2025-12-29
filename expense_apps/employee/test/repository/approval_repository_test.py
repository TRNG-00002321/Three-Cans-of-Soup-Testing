from unittest.mock import MagicMock
import pytest


class Test_Approval_Repository():
    
    def test_find_expenses_with_status_for_user(self, approval_repository, conn, cursor):
        cursor.fetchall.return_value = [
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
        
        conn.execute.assert_called_once()
        cursor.fetchall.assert_called_once()
        
        assert expense.id == 5
        assert expense.amount == 75.25
        assert expense.description == 'Taxi fare'
        assert expense.date == '2024-01-18'
        assert approval.status == 'denied'
        assert approval.comment == 'Test Comment 1'
        assert approval.review_date == '2024-01-19'
    
    #Look over _ fuinctionality
    def test_find_expenses_with_status_empty_result(self, approval_repository, conn, cursor):
        cursor.fetchall.return_value = []
        
        results = approval_repository.find_expenses_with_status_for_user(2)
        assert len(results) == 0
        
        conn.execute.assert_called_once()
        cursor.fetchall.assert_called_once()
    
    def test_find_expenses_with_status_connection_fails(self, approval_repository, db_connection, conn, cursor):
        db_connection.get_connection.side_effect = Exception("Database connection failed")
        
        with pytest.raises(Exception, match="Database connection failed"):
            approval_repository.find_expenses_with_status_for_user(2)
        
        db_connection.get_connection.assert_called_once()
        conn.execute.assert_not_called()
        
