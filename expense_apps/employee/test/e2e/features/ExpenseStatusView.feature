Feature: View Expense Status
  As a employee
  I want to view the status of my submitted expenses
  so that I know whether they are pending, approved, or denied

  #Background runs before EVERY Scenario in the feature
  #Useful in common pre-conditions
  Background:
    Given the user is logged in

  #Scenario is like a test case with a given/when/then
  Scenario: Successful viewing of submitted expenses
  #Given specifies a pre-condition
    Given the employee is on the dashboard screen

  #When describes the action(s) being tested
    When the user clicks view my expenses

  #Then describes the expected outcome
    Then the all expenses should be displayed