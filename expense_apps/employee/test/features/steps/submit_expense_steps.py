from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

from behave import  given, when, then
from behave.api.pending_step import StepNotImplementedError

BASE_URL = "http://127.0.0.1:5000"

@given("the employee is logged in")
def step_login(context):
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

    assert "app" in context.driver.current_url

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

    assert context.driver.current_url == BASE_URL+"/app"

@then(u'the submitted expense with {amount} and {description} is shown with a pending status')
def step_check_is_pending(context, amount, description):
    expenses = context.wait.until(
        EC.visibility_of_element_located((By.ID, "expenses-list"))
    )

    for expense in expenses:
        amount_found = False
        description_found = False
        expense_labels = expense.find_elements_by_tag_name("td")
        for texts in expense_labels:
            if texts.text == description:
                description_found = True
            if texts.text == amount:
                amount_found = True


@then(u'the submitted expense has the date the expense is submitted')
def step_check_today_date(context):
    raise StepNotImplementedError(u'Then the submitted expense has the date the expense is submitted')


@given(u'the employee selects a {date}')
def step_add_date(context, date):
    text_box = context.wait.until(
        EC.visibility_of_element_located((By.ID, "date"))
    )

    text_box.send_keys(date)


@given(u'the amount is missing')
def step_no_amount(context):
    raise StepNotImplementedError(u'Given the amount is missing')


@then(u'a message to fill in the missing field pops up')
def step_check_pop_up(context):
    raise StepNotImplementedError(u'Then a message to fill in the missing field pops up')


@given(u'the description is missing')
def step_no_description(context):
    raise StepNotImplementedError(u'Given the desciption is missing')


@given(u'the default date is deleted')
def step_no_date(context):
    raise StepNotImplementedError(u'Given the default date is deleted')


@given(u'the employee enters blank spaces')
def step_blank_description(context):
    raise StepNotImplementedError(u'Given the employee enters blank spaces')


@then(u'a error message pops up')
def step_check_error(context):
    raise StepNotImplementedError(u'Then a error message pops up')


@given(u'the employee enters {invalid expense}')
def step_invalid_amount(context, negative_amount):
    raise StepNotImplementedError(u'Given the employee enters <invalid expense>')





