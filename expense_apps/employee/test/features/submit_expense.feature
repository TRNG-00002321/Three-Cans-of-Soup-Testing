Feature: Expense Submitting
  As an authorized employee,
  I should be able to submit expenses
  So that track expenses and/or get reimbursement

  Background:
      Given the employee is logged in
      And the employee has click on Submit New Expense

  Scenario Outline: A expense with amount and description is submitted and can be seen in expenses
      Given the employee enters a amount <amount>
      And the employee enters a description "<description>"
      When the employee clicks submit expense
      Then the employee is redirected to the dashboard
      And the submitted expense with <amount> and <description> is shown with a pending status
      And the submitted expense has the date the expense is submitted
      Examples:
      |amount| description|
      |123   | test expense submission|
      |0.01  | test minimum           |

  Scenario Outline: A expense with amount, description, and date is submitted and can be seen in expenses
      Given the employee enters a amount <amount>
      And the employee enters a description "<description>"
      And the employee selects a "<date>"
      When the employee clicks submit expense
      Then the employee is redirected to the dashboard
      And the submitted expense with <amount> and "<description>" is shown with a pending status
      And the submitted date is "<date>"

      Examples:
      |amount| description| date |
      |123   | test expense submission with date| 01/01/2026 |
      |122.32 | test another date               | 02/28/2026 |


  Scenario Outline: The employee attempts to submit an expense with a missing field
      Given the <field> is missing
      When the employee clicks submit expense
      Then the missing <field> is not valid
      Examples:
      |field|
      |amount|
      |description|
      |date       |

  Scenario Outline: the employee attempts to submit an invalid expense
      Given the employee enters <invalid_expense>
      When the employee clicks submit expense
      Then a message pops up due to invalid amount
      Examples:
      |invalid_expense|
      |0.00           |
      |-12            |

