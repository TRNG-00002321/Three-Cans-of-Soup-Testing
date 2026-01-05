Feature: Employee Logout
  As a registered employee
  I want to logout

  Scenario: Successful login with valid credentials
    Given the employee is logged in and on the dashboard
    When the employee clicks the logout button
    Then the employee should be redirected to the login page
