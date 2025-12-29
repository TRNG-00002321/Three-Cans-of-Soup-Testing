import pytest
from contextlib import contextmanager
from unittest.mock import Mock
from repository.expense_repository import ExpenseRepository, DatabaseConnection
from unittest.mock import MagicMock
import pytest

from repository.approval_repository import ApprovalRepository

@pytest.fixture
def cursor():
    return Mock()

@pytest.fixture
def conn():
    return Mock()

@pytest.fixture
def mock_get_connection(conn):
    @contextmanager
    def get_connection():
        yield conn
    yield get_connection

@pytest.fixture
def db_connection():
    return Mock(spec=DatabaseConnection)

@pytest.fixture(autouse=True)
def mock_database_setup(db_connection, mock_get_connection, conn, cursor):
    db_connection.get_connection = mock_get_connection
    conn.execute.return_value = cursor

@pytest.fixture
def expense_repository(db_connection):
    return ExpenseRepository(db_connection)


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