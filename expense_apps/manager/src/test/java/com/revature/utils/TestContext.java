package com.revature.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestContext {
    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl = "http://localhost:5001";

    private static TestContext instance;

    private TestContext() {
        // Driver is initialized in initializeDriver called by Hooks
    }

    public static synchronized TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }
    
    public void initializeDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        
        this.driver = new FirefoxDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
    }
    
    public WebDriver getDriver() {
        return this.driver;
    }
    
    public void quitDriver() {
        if (this.driver != null) {
            this.driver.quit();
            this.driver = null;
        }
    }

    public WebDriverWait getWait() {
        return this.wait;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
