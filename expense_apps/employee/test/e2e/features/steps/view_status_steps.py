from behave import given, when, then
from behave.exception import StepNotImplementedError
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

BASE_URL = "http://localhost:5000"


@given("the user is logged in")
def log_in_user(context):
    context.driver = webdriver.Chrome()
    context.wait = WebDriverWait(context.driver, 10)

    context.driver.get(BASE_URL)

    username= context.wait.until(EC.visibility_of_element_located((By.ID, "username")))
    username.send_keys("employee1")
    password = context.wait.until(EC.visibility_of_element_located((By.ID, "password")))
    password.send_keys("password123")
    login = context.wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, "button[type='submit']")))
    login.click()
    heading = context.wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, "div[id='header'] h1")))
    assert "Dashboard" in heading.text

@given ("the employee is on the dashboard screen")
def dashboard_screen(context):
    heading = context.wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, "div[id='header'] h1")))
    assert "Dashboard" in heading.text

@when("the user clicks view my expenses")
def view_expenses(context):
    expenses_button = context.wait.until(EC.visibility_of_element_located((By.ID,"show-expenses")))
    expenses_button.click()

@then ("the all expenses should be displayed")
def expenses_displayed(context):
    assert context.driver.find_element(By.ID, "expenses-list").is_displayed()