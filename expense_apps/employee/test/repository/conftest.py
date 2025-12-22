import pytest
from contextlib import contextmanager
from unittest.mock import Mock
from repository.expense_repository import ExpenseRepository, DatabaseConnection

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