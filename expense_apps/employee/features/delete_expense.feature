Feature: Delete Expense
  As an employee
  I want to delete a pending expense
  So that I can correct mistakes before reviewal

  Background:
    Given the employee has logged in
    And the employee is on the expense page

  Scenario: Successful delete for pending expense
    When the employee chooses a pending expense
    And the employee chooses to delete
    Then the expense is removed

  Scenario: Failed update for reviewed expense
    When the employee chooses a non-pending expense
    Then they cannot delete the expense