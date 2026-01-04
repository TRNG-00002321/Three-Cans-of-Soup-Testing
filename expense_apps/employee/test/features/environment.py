from selenium import webdriver
from selenium.webdriver.support.wait import WebDriverWait

BASE_URL = "http://127.0.0.1:5000"

def before_all(context):
    # options = Selenium::WebDriver::Chrome::Options.new
    # options.add_preference('profile.password_manager_leak_detection', false)

    options = webdriver.ChromeOptions()

    #disable password manager
    options.add_experimental_option("prefs", {
    "credentials_enable_service": False,
    "profile.password_manager_enabled": False
})
    #pop up still shows but it shouldn't affect the tests
    options.add_argument("--disable-blink-features=AutomationControlled")

    context.driver = webdriver.Chrome(options = options)
    context.driver.get(BASE_URL)
    context.driver.maximize_window()
    context.driver.get(BASE_URL)
    context.wait = WebDriverWait(context.driver, 10)
