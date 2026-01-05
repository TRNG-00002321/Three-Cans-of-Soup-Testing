from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


from behave.api.pending_step import StepNotImplementedError
@given(u'the employee is logged in and on the dashboard')
def step_employee_logged_in(context):
    context.execute_steps(u'''
Given the employee is on the login page
When the employee enters username employee1
And the employee enters password password123
And clicks the login button
Then the employee should be redirected to the dashboard
    ''')

@given(u'the employee is on the dashboard page')
def step_employee_on_dashboard(context):
    context.wait.until(EC.url_contains('/app'))
    title = context.driver.title
    assert "Employee Expense Dashboard" in title


@when(u'the employee clicks the logout button')
def step_employee_clicks_logout_button(context):
    context.driver.find_element(By.ID, 'logout-btn').click()


@then(u'the employee should be redirected to the login page')
def step_impl(context):
    context.wait.until(EC.url_contains('/login'))
    title = context.driver.title
    assert "Employee Login" in title
