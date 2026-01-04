Feature: Expense Submitting
  As an authorized employee,
  I should be able to submit expenses
  So that track expenses and/or get reimbursement

  Background:
    Given the employee is logged in
    And the employee has click on Submit New Expense

  Scenario: A expense with amount and description is submitted and can be seen in expenses
    Given the employee enters a amount <amount>
    And the employee enters a description "<description>"
    When the employee clicks submit expense
    Then the employee is redirected to the dashboard
    And the submitted expense with <amount> and <description> is shown with a pending status
    And the submitted expense has the date the expense is submitted

    |amount| description|
    |123   | test expense submission|
    |0.01  | test minimum           |

  Scenario: A expense with amount, description, and date is submitted and can be seen in expenses
    Given the employee enters a amount <amount>
    And the employee enters a description "<description>"
    And the employee selects a "<date>"
    When the employee clicks submit expense
    Then the employee is redirected to the dashboard
    And the submitted expense with <amount> and <description> is shown with a pending status

    |amount| description| date |
    |123   | test expense submission with date| 01-01-2026 |
    |122.32 | test random date                | no date    |


  Scenario: The employee attempts to submit an expense with a missing field
    Given the amount is missing
    When the employee clicks submit expense
    Then a message to fill in the missing field pops up

    Given the description is missing
    When the employee clicks submit expense
    Then a message to fill in the missing field pops up

    Given the default date is deleted
    When the employee clicks submit expense
    Then a message to fill in the missing field pops up

    Given the employee enters blank spaces
    When the employee clicks submit expense
    Then a error message pops up

  Scenario: the employee attempts to submit an invalid expense
    Given the employee enters <invalid expense>
    When the employee clicks submit expense
    Then a error message pops up

