Feature: Update Expense
  As an employee
  I want to update a pending expense
  So that I can correct mistakes before reviewal

  Background:
    Given the employee has logged in
    And the employee is on the expense page

  Scenario: Successful update for pending expense
    When the employee chooses a pending expense
    And the employee chooses to edit
    And the employee enters a valid expense
    Then they see the updated expense

  Scenario: Failed update for reviewed expense
    When the employee chooses a non-pending expense
    Then they cannot edit the expense