from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

@given('the employee is on the login page')
def step_on_the_login_page(context):
    context.driver.get("http://localhost:5000/login")
    header = context.driver.find_element(By.TAG_NAME, "h2")
    assert header.text == "Employee Login"


@when(u'the employee enters username {username}')
def step_employee_enters_username(context, username):
    username_field = context.driver.find_element(By.ID, "username")
    username_field.clear()
    username_field.send_keys(username)


@when(u'the employee enters password {password}')
def step_employee_enters_password(context, password):
    password_field = context.driver.find_element(By.ID, "password")
    password_field.clear()
    password_field.send_keys(password)


@when(u'clicks the login button')
def step_employee_clicks_login_button(context):
    login_button = context.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']")))
    login_button.click()


@then(u'the employee should be redirected to the dashboard')
def step_employee_should_be_redirected_to_dashboard(context):
    context.wait.until(EC.url_contains("/app"))
    header = context.driver.find_element(By.TAG_NAME, "h1")
    assert "Employee Expense Dashboard" in header.text

@then(u'an error message should be displayed indicating invalid login')
def step_error_message_displayed(context):
    context.wait.until(EC.text_to_be_present_in_element((By.ID, "login-message"), "Invalid credentials"))
    login_message = context.driver.find_element(By.ID, "login-message")
    assert "Invalid credentials" in login_message.text
