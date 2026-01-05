Feature: Generate Reports
  As a Manager
  I want to be able to generate reports for all/specific employees
  so that I can organize and track expenses

  #Background runs before EVERY Scenario in the feature
  #Useful in common pre-conditions
  Background:
    Given the user is logged in

  #Scenario is like a test case with a given/when/then
  Scenario: Successful all expenses report is generated
  #Given specifies a pre-condition
    Given the manager is on the generate reports page

  #When describes the action(s) being tested
    When the user clicks all expenses report

  #Then describes the expected outcome
    Then the all expenses report should be generated

      #Scenario is like a test case with a given/when/then
  Scenario: Successful employee report is generated
  #Given specifies a pre-condition
    Given the manager is on the generate reports page

  #When describes the action(s) being tested
    When the user enters the employee id 1
    And the user clicks generate employee report

  #Then describes the expected outcome
    Then the employee expenses report should be generated

          #Scenario is like a test case with a given/when/then
  Scenario: Successful category report is generated
  #Given specifies a pre-condition
    Given the manager is on the generate reports page

  #When describes the action(s) being tested
    When the user enters the category "lunch"
    And the user clicks generate category report

  #Then describes the expected outcome
    Then the category expenses report should be generated

          #Scenario is like a test case with a given/when/then
  Scenario: Successful date range report is generated
  #Given specifies a pre-condition
    Given the manager is on the generate reports page

  #When describes the action(s) being tested
    When the user enters the start date "12-01-2024"
    And the user enters the end date "12-05-2024"
    And the user clicks generate date range report

  #Then describes the expected outcome
    Then the date range expenses report should be generated

          #Scenario is like a test case with a given/when/then
  Scenario: Successful pending report is generated
  #Given specifies a pre-condition
    Given the manager is on the generate reports page

  #When describes the action(s) being tested
    When the user clicks pending expenses report

  #Then describes the expected outcome
    Then the pending expenses report should be generated