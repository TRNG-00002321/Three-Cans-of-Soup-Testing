package com.revature.e2e.hooks;

import java.io.File;
import java.sql.SQLException;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.revature.Api.DummyDataLoader;
import com.revature.e2e.utils.TestContext;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

public class Hooks {

    private TestContext context;
    private DummyDataLoader dataLoader;

    public Hooks() {
        this.context = TestContext.getInstance();
        this.dataLoader = new DummyDataLoader();
    }

    @BeforeAll
    public static void globalSetup() {
        //path to the downloads folder for the reports
        //works best with the absolute path
        String DOWNLOAD_PATH ="/Users/andrew/Desktop/Revature/Project_1/expense_apps/manager/src/test/resources/downloads";
        File downloadDir = new File(DOWNLOAD_PATH);
        File[] files = downloadDir.listFiles();
        if(files!=null) {
            for (File file : files) {
                if(file.getName().endsWith(".csv")) {
                    file.delete();
                }
            }
        }
        System.out.println("Starting test execution...");
    }


    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("Starting scenario: " + scenario.getName());
        context.initializeDriver(false);
        try {
            dataLoader.restoreDatabase();
        } catch (SQLException e) {

        }

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