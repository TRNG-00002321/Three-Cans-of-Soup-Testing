import os
import threading
import time
from werkzeug.serving import make_server
from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.chrome.options import Options as ChromeOptions
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.support.ui import WebDriverWait

from webdriver_manager.firefox import GeckoDriverManager
from selenium.webdriver.firefox.options import Options as FirefoxOptions
from selenium.webdriver.firefox.service import Service as FirefoxService


# Import your app and database logic
from main import create_app
from repository.database import DatabaseConnection

# 1. Get the directory where environment.py is located
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 2. Construct paths relative to BASE_DIR
# Adjust the number of '..' based on how many levels up the SQL file actually is
TEST_DB_PATH = os.path.join(BASE_DIR, 'test_expense_manager.db')
SEED_SQL_PATH = os.path.join(BASE_DIR, 'seed_data_20241229.sql')

def before_all(context):
    """Global setup: Database initialization and Server start."""
    # 1. Database Setup
    if os.path.exists(TEST_DB_PATH):
        os.remove(TEST_DB_PATH)
    
    db_conn = DatabaseConnection(TEST_DB_PATH)
    db_conn.initialize_database()
    
    with open(SEED_SQL_PATH, 'r') as f:
        seed_sql = f.read()
    
    with db_conn.get_connection() as conn:
        conn.executescript(seed_sql)
        conn.commit()

    # 2. Environment Configuration
    os.environ['DATABASE_PATH'] = TEST_DB_PATH
    
    # 3. Start Flask Server in Background
    app = create_app()
    app.config['TESTING'] = True
    context.server = make_server('127.0.0.1', 5000, app, threaded=True)
    context.server_thread = threading.Thread(target=context.server.serve_forever)
    context.server_thread.daemon = True
    context.server_thread.start()
    
    time.sleep(0.5) # Wait for server to boot

def before_scenario(context, scenario):
    """Scenario setup: Browser initialization."""
    options = FirefoxOptions()
    if 'headless' in scenario.effective_tags:
        options.add_argument('--headless')
    # options.add_experimental_option('excludeSwitches', ['disable-popup-blocking'])
    
    options.add_argument('--window-size=1920,1080')
    service = FirefoxService(GeckoDriverManager().install())
    
    context.driver = webdriver.Firefox(service=service, options=options)
    context.driver.implicitly_wait(10)
    context.wait = WebDriverWait(context.driver, 10)

def after_scenario(context, scenario):
    """Cleanup after each scenario."""
    if scenario.status == 'failed' or scenario.status == 'error':
        os.makedirs('screenshots', exist_ok=True)
        context.driver.save_screenshot(f"screenshots/{scenario.name}.png")
    
    if hasattr(context, 'driver'):
        context.driver.quit()

def after_all(context):
    """Global cleanup: Shutdown server."""
    if hasattr(context, 'server'):
        context.server.shutdown()
        context.server_thread.join()
