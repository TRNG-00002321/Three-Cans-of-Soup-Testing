import os
from unittest.mock import Mock
import pytest

from flask import Flask
from repository import (
    DatabaseConnection, 
    UserRepository, 
    ExpenseRepository, 
    ApprovalRepository
)
from service import AuthenticationService, ExpenseService
from api import auth_bp, expense_bp
from contextlib import contextmanager
import sqlite3

TEMP_DATABASE = 'tmp_database.db'

@pytest.fixture(scope='session', autouse=True)
def database_setup():
    database = DatabaseConnection()
    conn = sqlite3.connect(TEMP_DATABASE)
    with database.get_connection() as data:
        # Create users table
        data.backup(conn)
    conn.close()
    yield 
    os.remove(TEMP_DATABASE)


@pytest.fixture
def db_connection():
    return DatabaseConnection(TEMP_DATABASE)

@pytest.fixture
def user_repository(db_connection):
    yield UserRepository(db_connection)

@pytest.fixture
def expense_repository(db_connection):
    yield ExpenseRepository(db_connection)

@pytest.fixture
def approval_repository(db_connection):
    yield ApprovalRepository(db_connection)

@pytest.fixture
def app(user_repository, expense_repository, approval_repository):
    app = 0
    yield app

@pytest.fixture
def base_url():
    return "http://127.0.0.1:5000"