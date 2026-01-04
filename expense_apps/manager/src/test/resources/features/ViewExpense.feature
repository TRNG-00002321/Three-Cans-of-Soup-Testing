Feature: Manager Can View Expenses
  As a manager
  I want to view pending expenses
  So that I can approve or deny expenses

Scenario: Manager views all expenses
  Given the manager is logged in
  And the manager is on the dashboard
  When the manager clicks on the all expenses button
  Then the manager should see a list of all expenses 
  
Scenario: Manager views pending expenses
  Given the manager is logged in
  And the manager is on the dashboard
  When the manager clicks on the pending expenses button
  Then the manager should see a list of pending expenses
  


