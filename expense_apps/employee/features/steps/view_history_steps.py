from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
import time


from behave.api.pending_step import StepNotImplementedError

@when('the employee changes the filter to {filter_type}')
def step_employee_changes_filter(context, filter_type):
    filter_dropdown = context.wait.until(EC.presence_of_element_located((By.ID, "status-filter")))
    select = Select(filter_dropdown)
    select.select_by_visible_text(filter_type)

@then('the employee should see a list of {expected_result}')
def step_employee_sees_list_of_expenses(context, expected_result):
    context.wait.until(EC.presence_of_element_located((By.ID, "expenses-list")))
    all_rows = context.driver.find_elements(By.CSS_SELECTOR, "#expenses-list table tr")
    
    expense_rows = all_rows[1:] if len(all_rows) > 1 else []
    
    expected = {
        "all expenses": [3,"Office supplies"],           
        "all pending expenses": [1,"Business lunch"],  
        "all approved expenses": [1,"Travel expense"], 
        "all denied expenses": [1,"Office supplies"]     
    }
    
    actual_count = len(expense_rows)
    assert actual_count == expected.get(expected_result)[0], f"Expected {expected.get(expected_result)[0]} rows for '{expected_result}', but found {actual_count}"
   
    if actual_count > 0:
        first_row = expense_rows[0]
        description_cells = first_row.find_elements(By.TAG_NAME, "td")
        assert len(description_cells) >= 3, "Row should have at least 3 columns"
        description_text = description_cells[2].text.strip()
        assert description_text == expected.get(expected_result)[1], f"Expected {expected.get(expected_result)[1]} description for '{expected_result}', but found {description_text}"