Feature: Manager can login and logout
  As a Manager
  I want to be able to login and log out
  So that access to expense approval and denials are controlled

  Scenario: Manager successfully logs in
    Given the application is running and manager is on the login page
    When the manager inputs their credentials
    Then they should see the expenses dashboard

  Scenario: Manager fails to login
    Given the application is running and manager is on the login page
    When the user inputs incorrect credentials
    Then they should see and invalid credentials message

  Scenario: Manager logs out
    Given the manager is already logged in
    When the manager logs out
    Then they should be redirected to login page