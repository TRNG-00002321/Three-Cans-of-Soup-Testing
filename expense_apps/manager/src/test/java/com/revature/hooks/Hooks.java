package com.revature.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.revature.utils.TestContext;

public class Hooks {

    private TestContext context;

    public Hooks() {
        this.context = TestContext.getInstance();
    }

    @BeforeAll
    public static void globalSetup() {
        System.out.println("Starting test execution...");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("Starting scenario: " + scenario.getName());
        context.initializeDriver(false);
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Take screenshot on failure
        if (scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) context.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }

        // Clean up
        context.quitDriver();
    }

    @AfterAll
    public static void globalTeardown() {
        System.out.println("Test execution complete.");
    }
}
