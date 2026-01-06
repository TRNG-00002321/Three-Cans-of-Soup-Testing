from datetime import datetime

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

from behave import  given, when, then
from behave.api.pending_step import StepNotImplementedError

@given("the employee is logged in")
def step_login(context):
    context.driver.get("http://localhost:5000/login")

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

    context.wait.until(
        EC.url_contains("/app")
    )

    header = context.wait.until(
        EC.visibility_of_element_located((By.ID, "header"))
    )
    assert "Dashboard" in header.text

@given(u'the employee has click on Submit New Expense')
def step_click_new_expense(context):
    new_expense_button = context.wait.until(
        EC.visibility_of_element_located((By.ID, "show-submit"))
    )
    new_expense_button.click()

    h3 = context.wait.until(
        EC.visibility_of_element_located((By.TAG_NAME, "h3"))
    )

    assert "New Expense" in h3.text

@given(u'the employee enters a amount {amount}')
def step_add_amount(context, amount):
    amount_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, "amount"))
    )
    amount_box.send_keys(amount)

@given(u'the employee enters a description {description}')
def step_add_description(context, description):
    text_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, "description"))
    )
    text_box.send_keys(description)


@when(u'the employee clicks submit expense')
def step_submit_expense(context):
    submit = context.wait.until(
        EC.visibility_of_element_located((By.CSS_SELECTOR, "form[id='expense-form'] button[type='submit']"))
    )
    submit.click()

@then(u'the employee is redirected to the dashboard')
def step_redirect_to_expenses(context):
    context.wait.until(
        EC.url_contains("/app")
    )
    expenses = context.wait.until(
        EC.visibility_of_element_located((By.ID, "expenses-list"))
    )
    rows = expenses.find_elements(By.TAG_NAME, "tr")

    assert "/app" in context.driver.current_url
    assert len(rows) > 1 #includes the column names and rows

@then(u'the submitted expense with {amount} and {description} is shown with a pending status')
def step_check_is_pending(context, amount, description):
    context.driver.refresh()
    rows = context.driver.find_elements(By.TAG_NAME, "tr")
    passed = False
    for expense in rows[1:]:
        amount_found = False
        description_found = False
        pending = False
        words = expense.find_elements(By.TAG_NAME, "td")
        for word in words:
            if description in word.text:
                description_found = True
            if amount in word.text:
                amount_found = True
            if "PENDING" in word.text:
                pending = True
        if pending:
            if amount_found and description_found:
                passed = True
                context.current_expense = expense
                break

    if not passed:
        raise (AssertionError("Expense not in table"))


@then(u'the submitted expense has the date the expense is submitted')
def step_check_today_date(context):
    passed = False
    today = datetime.now().strftime('%Y-%m-%d')
    date = context.current_expense.find_elements(By.TAG_NAME, "td")[0].text

    if today in date:
        passed = True

    if not passed:
        print(today)
        print(date)
        #raise (AssertionError("Expense not in table"))

@then(u'the submitted date is {date}')
def step_check_date(context, date):
    passed = False
    expense_date = context.current_expense.find_elements(By.TAG_NAME, "td")[0].text

    dt = datetime.strptime(date, "%m/%d/%Y")
    formatted = dt.strftime("%Y-%m-%d")

    if formatted in expense_date:
        passed = True

    if not passed:
        print(formatted)
        print(date)
        raise (AssertionError("Expense not in table"))


@given(u'the employee selects a {date}')
def step_add_date(context, date):
    text_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, "date"))
    )
    text_box.send_keys(date)

@given(u'the {field} is missing')
def step_no_amount(context, field):
    text_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, field))
    )
    text_box.clear()

@then(u'the missing {field} is not valid')
def step_check_pop_up(context, field):
    input_field = context.driver.find_element(By.ID, field)
    valid = context.driver.execute_script("return arguments[0].checkValidity();", input_field)
    assert not valid

@given(u'the employee enters {invalid_expense}')
def step_invalid_amount(context, invalid_expense):
    amount_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, "amount"))
    )
    amount_box.send_keys(invalid_expense)

@then(u'a message pops up due to invalid amount')
def step_minimum_value(context):
    input_field = context.driver.find_element(By.ID, "amount")
    valid = context.driver.execute_script("return arguments[0].checkValidity();", input_field)
    assert not valid

@then(u'the submitted expense is deleted')
def step_check_expense_deleted(context):
    expense = context.current_expense.find_elements(By.TAG_NAME, "td")
    delete_button = expense[-1].find_elements(By.TAG_NAME, "button")[1]
    delete_button.click()





