from unittest.mock import MagicMock
import pytest

from repository.approval_repository import ApprovalRepository


@pytest.fixture
def mock_db_connection():
    """Mock DatabaseConnection with context manager support."""
    mock_conn = MagicMock()
    mock_context = MagicMock()
    
    # Setup context manager to return the connection itself
    mock_conn.get_connection.return_value.__enter__.return_value = mock_context
    mock_conn.get_connection.return_value.__exit__.return_value = None
    
    return mock_conn, mock_context

@pytest.fixture
def approval_repository(mock_db_connection):
    """Create ApprovalRepository with mocked database connection."""
    mock_conn, _ = mock_db_connection
    return ApprovalRepository(mock_conn)