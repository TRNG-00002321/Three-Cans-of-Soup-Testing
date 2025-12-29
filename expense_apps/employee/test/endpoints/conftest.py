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

@pytest.fixture
def database():
    with sqlite3.connect(':memory:') as conn:
        # Create users table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            )
        ''')
        
        # Create expenses table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                description TEXT NOT NULL,
                date TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (id)
            )
        ''')
        
        # Create approvals table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS approvals (
                id INTEGER PRIMARY KEY,
                expense_id INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                reviewer INTEGER,
                comment TEXT,
                review_date TEXT,
                FOREIGN KEY (expense_id) REFERENCES expenses (id),
                FOREIGN KEY (reviewer) REFERENCES users (id)
            )
        ''')
        
        conn.commit()

        yield conn


@pytest.fixture
def db_connection(database):
    @contextmanager
    def get_connection():
        yield database
    db_connection =  Mock(spec=DatabaseConnection)

    db_connection.get_connection = get_connection
    return db_connection

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