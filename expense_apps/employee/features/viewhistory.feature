Feature: Employee View history of approved/denied expenses
  As a registered employee
  I want to be able to view the history of my approved/denied expenses

  Scenario Outline: Employee views history of expenses by filter
    Given the employee is logged in and on the dashboard
    When the employee changes the filter to <filter_type>
    Then the employee should see a list of <expected_result>

    Examples:
      | filter_type | expected_result           |
      | All         | all expenses              |
      | Pending     | all pending expenses      |
      | Denied      | all denied expenses       |
      | Approved    | all approved expenses     |