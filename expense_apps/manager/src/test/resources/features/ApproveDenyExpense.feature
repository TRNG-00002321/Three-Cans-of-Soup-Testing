Feature: Manager Can Approve or Deny Expenses
  As a manager
    I want to be able to approve or deny expenses

  Scenario: Manager approves an expense
    Given the manager is logged in
    And the manager is on the dashboard
    And there is at least one pending expense
    And the manager clicks on the review button for a specific expense
    When the manager clicks on approve button in the expense review popup
    Then the expense status should be updated to approved
    And the manager should see a confirmation message at the bottom of the expense review popup
    And the approved expense should no longer appear in the pending expenses list

  Scenario: Manager approves an expense
    Given the manager is logged in
    And the manager is on the dashboard
    And there is at least one pending expense
    And the manager clicks on the review button for a specific expense
    When the manager clicks on deny button in the expense review popup
    Then the expense status should be updated to denied
    And the manager should see a confirmation message at the bottom of the expense review popup
    And the denied expense should no longer appear in the pending expenses list
