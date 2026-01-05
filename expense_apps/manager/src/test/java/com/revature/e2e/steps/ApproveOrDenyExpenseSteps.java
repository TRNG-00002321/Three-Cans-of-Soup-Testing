package com.revature.e2e.steps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.revature.e2e.utils.TestContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ApproveOrDenyExpenseSteps {

    private TestContext context;
    private String reviewedExpenseId;

    public ApproveOrDenyExpenseSteps() {
        this.context = TestContext.getInstance();
    }

    @Given("there is at least one pending expense")
    public void there_is_at_least_one_pending_expense() {
        context.getDriver().findElement(By.id("show-pending")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("pending-expenses-list")));
        WebElement table = context.getDriver().findElement(By.id("pending-expenses-list"));
        List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
        assertTrue(!rows.isEmpty(), "No pending expenses found");
    }

    @Given("the manager clicks on the review button for a specific expense")
    public void the_manager_clicks_on_the_review_button_for_a_specific_expense() {
        WebElement table = context.getDriver().findElement(By.id("pending-expenses-list"));
        List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
        WebElement firstRow = rows.get(0);
        WebElement reviewButton = firstRow.findElement(By.xpath("//tbody/tr[2]/td[5]/button"));
        reviewButton.click();

        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("review-modal")));
    }

    @When("the manager clicks on approve button in the expense review popup")
    public void the_manager_clicks_on_approve_button_in_the_expense_review_popup() {
        WebElement approveButton = context.getDriver().findElement(By.id("approve-expense"));
        approveButton.click();
    }

    @When("the manager clicks on deny button in the expense review popup")
    public void the_manager_clicks_on_deny_button_in_the_expense_review_popup() {
        WebElement denyButton = context.getDriver().findElement(By.id("deny-expense"));
        denyButton.click();
    }

    @Then("the expense status should be updated to approved")
    public void the_expense_status_should_be_updated_to_approved() {
        context.getWait().until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("review-message"), "approved"));
        WebElement messageElement = context.getDriver().findElement(By.id("review-message"));
        assertTrue(messageElement.getText().toLowerCase().contains("approved"));
    }

    @Then("the expense status should be updated to denied")
    public void the_expense_status_should_be_updated_to_denied() {
        context.getWait().until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("review-message"), "denied"));
        WebElement messageElement = context.getDriver().findElement(By.id("review-message"));
        assertTrue(messageElement.getText().toLowerCase().contains("denied"));
    }

    @Then("the manager should see a confirmation message at the bottom of the expense review popup")
    public void the_manager_should_see_a_confirmation_message_at_the_bottom_of_the_expense_review_popup() {
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("review-message")));
        WebElement confirmationMessage = context.getDriver().findElement(By.id("review-message"));
        assertTrue(confirmationMessage.isDisplayed());
        assertFalse(confirmationMessage.getText().isEmpty());
    }

    @Then("the approved expense should no longer appear in the pending expenses list")
    public void the_approved_expense_should_no_longer_appear_in_the_pending_expenses_list() {
        context.getWait().until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-modal")));

        context.getDriver().findElement(By.id("refresh-pending")).click();
        context.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("pending-expenses-list")));

        WebElement table = context.getDriver().findElement(By.id("pending-expenses-list"));
        List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (!cells.isEmpty()) {
                String firstCellText = cells.get(0).getText();
                assertFalse(firstCellText.contains("testuser (ID: 5)"),
                        "Approved expense still appears in pending expenses list");
            }
        }
    }

    @Then("the denied expense should no longer appear in the pending expenses list")
    public void the_denied_expense_should_no_longer_appear_in_the_pending_expenses_list() {
        context.getWait().until(ExpectedConditions.invisibilityOfElementLocated(By.id("review-modal")));

        WebElement table = context.getDriver().findElement(By.id("pending-expenses-list"));
        List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (!cells.isEmpty()) {
                String firstCellText = cells.get(0).getText();
                assertFalse(firstCellText.contains("testuser (ID: 5)"),
                        "Denied expense still appears in pending expenses list");
            }
        }
    }
}
