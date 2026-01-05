Feature: Employee Login
  As a registered employee
  I want to login
  So that I can securely access the expense portal

  Background:
    Given the employee is on the login page

  Scenario: Successful login with valid credentials
    When the employee enters username employee1
    And the employee enters password password123
    And clicks the login button
    Then the employee should be redirected to the dashboard

  Scenario Outline: Unsuccessful login with invalid credentials
    When the employee enters username <username>
    And the employee enters password <password>
    And clicks the login button
    Then an error message should be displayed indicating invalid login

    Examples:
      | username    | password    |
      | invaliduser | invalidpass |
      |nousername   | nopassword   |

  Scenario: Unsuccessful login with manager credentials
    When the employee enters username manager1
    And the employee enters password password123
    And clicks the login button
    Then an error message should be displayed indicating login failed
