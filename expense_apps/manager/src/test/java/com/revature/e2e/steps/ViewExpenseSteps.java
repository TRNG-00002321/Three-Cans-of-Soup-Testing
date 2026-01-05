package com.revature.e2e.steps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.revature.e2e.utils.TestContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ViewExpenseSteps {

    private TestContext context;

    public ViewExpenseSteps() {
        this.context = TestContext.getInstance();
    }
    // public ViewExpenseSteps(TestContext context) {
    //     this.context = context;
    // }

    @Given("the manager is logged in")
    public void the_manager_is_logged_in() {
        context.getDriver().get(context.getBaseUrl() + "/login.html");
        context.getDriver().findElement(By.id("username")).sendKeys("manager1");
        context.getDriver().findElement(By.id("password")).sendKeys("password123");
        context.getDriver().findElement(By.cssSelector("button[type='submit']")).click();

    }

    @Given("the manager is on the dashboard")
    public void the_manager_is_on_the_dashboard() {
        context.getWait().until(ExpectedConditions.urlContains("/manager"));
        WebElement header = context.getDriver().findElement(By.tagName("h1"));
        assertTrue(header.getText().contains("Manager Expense Dashboard"));
        assertTrue(header.isDisplayed());
    }

    @When("the manager clicks on the pending expenses button")
    public void the_manager_clicks_on_the_pending_expenses_button() {
        context.getDriver().findElement(By.id("show-pending")).click();
    }

    @Then("the manager should see a list of pending expenses")
    public void the_manager_should_see_a_list_of_pending_expenses() {
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#pending-expenses-list table")));
        WebElement table = context.getDriver().findElement(By.cssSelector("#pending-expenses-list table"));
        assertTrue(table.isDisplayed());
        List<WebElement> columns = table.findElements(By.tagName("th"));
        assertTrue(columns.size() > 1);
        assertTrue(columns.get(0).getText().contains("Employee"));
        assertTrue(columns.get(1).getText().contains("Date"));
        assertTrue(columns.get(2).getText().contains("Amount"));
        assertTrue(columns.get(3).getText().contains("Description"));
    }

    @When("the manager clicks on the all expenses button")
    public void the_manager_clicks_on_the_all_expenses_button() {
        context.getDriver().findElement(By.id("show-all-expenses")).click();
    }

    @Then("the manager should see a list of all expenses")
    public void the_manager_should_see_a_list_of_all_expenses() {
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#all-expenses-list table")));
        WebElement table = context.getDriver().findElement(By.cssSelector("#all-expenses-list table"));
        assertTrue(table.isDisplayed());
        List<WebElement> columns = table.findElements(By.tagName("th"));
        assertTrue(columns.size() > 1);
        assertTrue(columns.get(0).getText().contains("Employee"));
        assertTrue(columns.get(1).getText().contains("Date"));
        assertTrue(columns.get(2).getText().contains("Amount"));
        assertTrue(columns.get(3).getText().contains("Description"));
        assertTrue(columns.get(4).getText().contains("Status"));
        assertTrue(columns.get(5).getText().contains("Reviewer"));
        assertTrue(columns.get(6).getText().contains("Comment"));
    }
}
