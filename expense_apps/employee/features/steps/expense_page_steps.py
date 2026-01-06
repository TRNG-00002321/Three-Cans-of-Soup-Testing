from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

@given('the employee has logged in')
def step_employee_has_logged_in(context):
    context.driver.get("http://localhost:5000/login")
    username_field = context.driver.find_element(By.ID, "username")
    password_field = context.driver.find_element(By.ID, "password")
    username_field.send_keys("employee1")
    password_field.send_keys("password123")
    login_button = context.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']")))
    login_button.click()
    context.wait.until(EC.url_contains("/app"))


@given('the employee is on the expense page')
def step_employee_is_on_expense_page(context):
    # ensure the expense list is present before operating on rows
    context.wait.until(EC.presence_of_element_located((By.ID, "expenses-list")))

@when('the employee chooses a pending expense')
def step_employee_chooses_a_pending_expense(context):
    all_rows = context.driver.find_elements(By.CSS_SELECTOR, "#expenses-list table tr")

    # Choose the first pending expense from the pre-captured rows in context
    for row in all_rows[1:]:
        
        try:
            status = row.find_element(By.XPATH, "./td[4]").text.strip()
        except Exception:
            continue
        if status.upper() == 'PENDING':
            context.expense = row
            return
            
    assert False, 'No pending expense was found'

@when('the employee chooses a non-pending expense')
def step_employee_chooses_non_pending_expense(context):
    all_rows = context.driver.find_elements(By.CSS_SELECTOR, "#expenses-list table tr")

    # Choose the first pending expense from the pre-captured rows in context
    for row in all_rows[1:]:
        
        try:
            status = row.find_element(By.XPATH, "./td[4]").text.strip()
        except Exception:
            continue
        if status.upper() != 'PENDING':
            context.expense = row
            return
            
    assert False, 'No non-pending expense was found'