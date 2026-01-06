package com.revature.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.revature.e2e.utils.TestContext;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManagerAuthSteps {

    private TestContext context;
    private WebDriver driver;

    public ManagerAuthSteps() {
        this.context = TestContext.getInstance();
    }

    @Before
    public void setUpClass() {
        if(context == null) {
            this.context = TestContext.getInstance();
            context.initializeDriver(true);
        }
    }

    @After
    public void afterScenario() {
        context.quitDriver();
    }

    @Given("the application is running and manager is on the login page")
    public void the_application_is_running_and_manager_is_on_the_login_page() {
        context.getDriver().get(context.getBaseUrl() + "/login.html");

        String title = context.getDriver().getTitle();
        assertTrue(title.contains("Manager Login"));
    }
    @When("the manager inputs their credentials")
    public void the_manager_inputs_their_credentials() {
        context.getDriver().findElement(By.id("username")).sendKeys("manager1");
        context.getDriver().findElement(By.id("password")).sendKeys("password123");
        context.getDriver().findElement(By.cssSelector("button[type='submit']")).click();
    }
    @Then("they should see the expenses dashboard")
    public void they_should_see_the_expenses_dashboard() {
        context.getWait().until(ExpectedConditions.urlContains("/manager"));
        WebElement header = context.getDriver().findElement(By.tagName("h1"));
        assertTrue(header.getText().contains("Manager Expense Dashboard"));
        assertTrue(header.isDisplayed());
    }

    @When("the user inputs incorrect credentials")
    public void the_user_inputs_incorrect_credentials() {
        context.getDriver().findElement(By.id("username")).sendKeys("manager1");
        context.getDriver().findElement(By.id("password")).sendKeys("wrongPassword");
        context.getDriver().findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Then("they should see and invalid credentials message")
    public void they_should_see_and_invalid_credentials_message() {
        WebElement message_location = context.getDriver().findElement(By.id("login-message"));
        WebElement message = context.getWait().until(
                ExpectedConditions.visibilityOf(message_location.findElement(By.tagName("p")))
        );
        assertTrue(message.getText().contains("Invalid credentials"));
    }

    @Given("the manager is already logged in")
    public void the_manager_is_already_logged_in() {
        context.getDriver().get(context.getBaseUrl() + "/login.html");
        context.getDriver().findElement(By.id("username")).sendKeys("manager1");
        context.getDriver().findElement(By.id("password")).sendKeys("password123");
        context.getDriver().findElement(By.cssSelector("button[type='submit']")).click();
    }

    @When("the manager logs out")
    public void the_manager_logs_out() {
        context.getDriver().findElement(By.id("logout-btn")).click();
    }

    @Then("they should be redirected to login page")
    public void they_should_be_redirected_to_login_page() {
        assertTrue(context.getDriver().getCurrentUrl().contains("login.html"));
    }
}