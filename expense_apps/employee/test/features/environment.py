from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

BASE_URL = "http://127.0.0.1:5000"

def before_all(context):
    # options = Selenium::WebDriver::Chrome::Options.new
    # options.add_preference('profile.password_manager_leak_detection', false)

    options = webdriver.ChromeOptions()

    #disable password manager
    options.add_experimental_option("prefs", {
    "credentials_enable_service": False,
    "profile.password_manager_enabled": False,
    "profile.password_manager_leak_detection": False
})
    options.add_argument("--disable-blink-features=AutomationControlled")

    context.driver = webdriver.Chrome(options = options)
    context.driver.get(BASE_URL)
    context.driver.maximize_window()
    context.wait = WebDriverWait(context.driver, 10)

def before_scenario(context, scenario):
    username = context.wait.until(
        EC.visibility_of_element_located((By.ID, "username"))
    )
    username.clear()
    username.send_keys("employee1")

    password = context.wait.until(
        EC.visibility_of_element_located((By.ID, "password"))
    )
    password.clear()
    password.send_keys("password123")

    login_button = context.wait.until(
        EC.visibility_of_element_located((By.TAG_NAME, "button"))
    )
    login_button.click()

def after_scenario(context, scenario):
    logout = context.wait.until(
        EC.visibility_of_element_located((By.ID, "logout-btn"))
    )
    logout.click()


def after_all(context):
    context.driver.close()
    context.driver.quit()
