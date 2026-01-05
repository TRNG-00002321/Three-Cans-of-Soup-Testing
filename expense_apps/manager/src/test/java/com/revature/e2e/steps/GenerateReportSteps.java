package com.revature.e2e.steps;

import com.revature.e2e.utils.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerateReportSteps {
    private static final String DOWNLOAD_PATH ="/Users/andrew/Desktop/Revature/Project_1/expense_apps/manager/src/test/resources/downloads";
    //private WebDriver driver;
    private final String BASE_URL = "http://localhost:5001";
    //WebDriverWait wait;
    private TestContext context;

    public GenerateReportSteps() {
        this.context = TestContext.getInstance();
    }





    @Given("the user is logged in")
    public void theUserIsLoggedIn() {
        // Write code here that turns the phrase above into concrete actions

        context.getDriver().get(BASE_URL);
        context.getDriver().findElement(By.id("username")).sendKeys("manager1");
        context.getDriver().findElement(By.id("password")).sendKeys("password123");
        context.getDriver().findElement(By.xpath("//button[@type='submit']")).click();
        WebElement result = context.getWait().until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[id='header'] h1"))
        );
        assertTrue(result.getText().contains("Dashboard"));
    }

    @Given("the manager is on the generate reports page")
    public void theManagerIsOnTheGenerateReportsPage() {
        // Write code here that turns the phrase above into concrete actions
        context.getDriver().get(BASE_URL);
        context.getDriver().findElement(By.id("show-reports")).click();
        assertTrue(context.getDriver().findElement(By.id("generate-all-expenses-report")).isDisplayed());
    }

    @When("the user clicks all expenses report")
    public void theUserClicksAllExpensesReport() {
        // Write code here that turns the phrase above into concrete actions
        context.getDriver().findElement(By.id("generate-all-expenses-report")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[normalize-space()='Report generated successfully!']")));
    }

    @Then("the all expenses report should be generated")
    public void allExpensesReportShouldBeGenerated() {
        // Write code here that turns the phrase above into concrete actions
        File downloadedReport = new File(DOWNLOAD_PATH + "/all_expenses_report.csv");
        assertTrue(downloadedReport.exists());
    }

    @When ("the user enters the employee id {int}")
    public void theUserEntersTheEmployeeId(int id) {
        context.getDriver().findElement(By.id("employee-report-id")).sendKeys(String.valueOf(id));
    }
    @And("the user clicks generate employee report")
    public void theUserClicksGenerateEmployeeReport() {
        context.getDriver().findElement(By.id("generate-employee-report")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[normalize-space()='Report generated successfully!']")));

    }

    @Then ("the employee expenses report should be generated")
    public void employeeExpensesReportShouldBeGenerated() {
        File downloadedReport = new File(DOWNLOAD_PATH + "/employee_1_report.csv");
        assertTrue(downloadedReport.exists());
    }


    @When ("the user enters the category {string}")
    public void theUserEntersTheCategoryLunch(String category) {
        context.getDriver().findElement(By.id("category-report")).sendKeys(category);
    }
    @And ("the user clicks generate category report")
    public void theUserClicksGenerateCategoryReport() {
        context.getDriver().findElement(By.id("generate-category-report")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[normalize-space()='Report generated successfully!']")));

    }

    @Then ("the category expenses report should be generated")
    public void categoryExpensesReportShouldBeGenerated() {
        File downloadedReport = new File(DOWNLOAD_PATH + "/category_lunch_report.csv");
        assertTrue(downloadedReport.exists());
    }


    @When ("the user enters the start date {string}")
    public void theUserEntersTheStartDateString(String startDate) {
        context.getDriver().findElement(By.id("start-date")).sendKeys(startDate);
    }

    @And ("the user enters the end date {string}")
    public void theUserEntersTheEndDateString(String endDate) {
        context.getDriver().findElement(By.id("end-date")).sendKeys(endDate);
    }

    @And ("the user clicks generate date range report")
    public void theUserClicksGenerateDateRangeReport() {

        context.getDriver().findElement(By.id("generate-date-range-report")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[normalize-space()='Report generated successfully!']")));

    }

    @Then ("the date range expenses report should be generated")
    public void dateRangeExpensesReportShouldBeGenerated() {
        File downloadedReport = new File(DOWNLOAD_PATH + "/expenses_2024-12-01_to_2024-12-05_report.csv");
        assertTrue(downloadedReport.exists());
    }

    @When ("the user clicks pending expenses report")
    public void theUserClicksPendingExpensesReport() {
        context.getDriver().findElement(By.id("generate-pending-report")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[normalize-space()='Report generated successfully!']")));

    }

    @Then("the pending expenses report should be generated")
    public void pendingExpensesReportShouldBeGenerated() {
        // Write code here that turns the phrase above into concrete actions
        File downloadedReport = new File(DOWNLOAD_PATH + "/pending_expenses_report.csv");
        assertTrue(downloadedReport.exists());
    }
}
