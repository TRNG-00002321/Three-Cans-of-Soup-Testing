from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager

import os


def before_scenario(context, scenario):
    """Initialize WebDriver before each scenario."""
    options = Options()
    options.add_argument('--headless')
    options.add_argument('--window-size=1920,1080')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    
    service = Service(ChromeDriverManager().install())
    context.driver = webdriver.Chrome(service=service, options=options)
    context.wait = WebDriverWait(context.driver, 10)


def after_scenario(context, scenario):
    """Cleanup after each scenario."""
    if scenario.status == 'failed':
        os.makedirs('screenshots', exist_ok=True)
        context.driver.save_screenshot(
            f"screenshots/{scenario.name.replace(' ', '_')}.png"
        )
    
    if hasattr(context, 'driver'):
        context.driver.quit()
