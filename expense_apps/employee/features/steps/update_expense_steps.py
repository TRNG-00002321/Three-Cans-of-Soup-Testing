from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

@when('the employee chooses to edit')
def step_employee_chooses_to_edit(context):
    edit_buttons = context.expense.find_elements(By.XPATH, ".//button[normalize-space()='Edit']")
    if edit_buttons:
        edit_buttons[0].click()
        context.wait.until(EC.presence_of_element_located((By.ID, "edit-expense-section")))
        return
    
    assert False, 'No Edit button was found'


@when('the employee enters a valid expense')
def step_employee_enters_valid_expense(context):
    amount_field = context.driver.find_element(By.ID, "edit-amount")
    description_field = context.driver.find_element(By.ID, "edit-description")
    amount_field.clear()
    amount_field.send_keys("100.00")
    description_field.clear()
    description_field.send_keys("Updated expense description")
    submit_button = context.driver.find_element(By.CSS_SELECTOR, "form[id='edit-expense-form'] button[type='submit']")
    submit_button.click()


@then('they see the updated expense')
def step_they_see_updated_expense(context):
    context.driver.find_element(By.ID, "show-expenses").click()
    context.wait.until(EC.presence_of_element_located((By.ID, "expenses-list")))
    all_rows = context.driver.find_elements(By.CSS_SELECTOR, "#expenses-list table tr")

    # Choose the first pending expense from the pre-captured rows in context
    for row in all_rows[1:]:
        
        try:
            status = row.find_element(By.XPATH, "./td[4]").text.strip()
        except Exception:
            continue
        if status.upper() == 'PENDING':
            assert "$100.00" == row.find_element(By.XPATH, "./td[2]").text.strip(), "Amount does not match"
            assert row.find_element(By.XPATH, "./td[3]").text.strip().find("Updated"), "Description does not match"
            return
    
    assert False, 'No pending expense was found'


@then('they cannot edit the expense')
def step_they_cannot_edit_expense(context):
    # After selecting a non-pending row, ensure there is no Edit button in that row
    edit_buttons = context.expense.find_elements(By.XPATH, ".//button[normalize-space()='Edit']")
    assert len(edit_buttons) == 0