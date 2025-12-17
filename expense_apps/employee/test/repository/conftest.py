import pytest
from contextlib import contextmanager
from repository import ExpenseRepository, DatabaseConnection

@pytest.fixture
def cursor(mocker):
    return mocker.Mock()

@pytest.fixture
def conn(mocker):
    return mocker.MagicMock()

@pytest.fixture
def mock_get_connection(conn):
    @contextmanager
    def get_connection():
        yield conn
    yield get_connection

@pytest.fixture
def db_connection(mocker):
    return mocker.Mock(spec=DatabaseConnection)

@pytest.fixture
def expense_repository(db_connection):
    return ExpenseRepository(db_connection)