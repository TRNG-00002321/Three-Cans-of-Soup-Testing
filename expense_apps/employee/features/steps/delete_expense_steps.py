from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC


@when('the employee chooses to delete')
def step_employee_chooses_to_delete(context):
    delete_buttons = context.expense.find_elements(By.XPATH, ".//button[normalize-space()='Delete']")
    if delete_buttons:
        delete_buttons[0].click()
        # accept the confirm dialog
        try:
            alert = context.wait.until(EC.alert_is_present())
            alert.accept()
        except Exception:
            pass
        # accept the success/error alert dialog
        try:
            alert = context.wait.until(EC.alert_is_present())
            alert.accept()
        except Exception:
            pass
        return

    assert False, 'No Delete button was found'


@then('the expense is removed')
def step_they_see_deleted_expense(context):
    context.wait.until(EC.presence_of_element_located((By.ID, "expenses-list")))
    all_rows = context.driver.find_elements(By.CSS_SELECTOR, "#expenses-list table tr")

    # Assert that no more expenses have the pending status
    for row in all_rows[1:]:
        
        try:
            status = row.find_element(By.XPATH, "./td[4]").text.strip()
        except Exception:
            continue
        if status.upper() == 'PENDING':
            assert False, "Pending Expense exists"
            return
    
    assert True


@then('they cannot delete the expense')
def step_they_cannot_delete_expense(context):
    delete_buttons = context.expense.find_elements(By.XPATH, ".//button[normalize-space()='Delete']")
    assert len(delete_buttons) == 0
