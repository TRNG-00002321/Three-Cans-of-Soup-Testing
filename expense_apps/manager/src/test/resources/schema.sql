-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

-- Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    description TEXT NOT NULL,
    date TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Create approvals table
CREATE TABLE IF NOT EXISTS approvals (
    id INTEGER PRIMARY KEY,
    expense_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    reviewer INTEGER,
    comment TEXT,
    review_date TEXT,
    FOREIGN KEY (expense_id) REFERENCES expenses (id),
    FOREIGN KEY (reviewer) REFERENCES users (id)
);
