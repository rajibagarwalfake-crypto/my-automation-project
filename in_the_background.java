package com.nauklri;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class in_the_background {
    // Configuration - update these values as needed
    private static final String USERNAME = "dibya.rinku96@gmail.com";
    private static final String PASSWORD = "Yash@123456";
    private static final String RESUME_PATH = "C:\\Users\\Soumya\\Downloads\\pdfs only\\Dibyajyoti_Behera_Resume_9_10.docx";
    private static final int START_HOUR = 8;  // 8 AM
    private static final int END_HOUR = 19;   // 7 PM
    private static final int INTERVAL_MINUTES = 10;

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            if (isWithinScheduledTime()) {
                runNaukriAutomation();
            }
        }, 0, INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private static boolean isWithinScheduledTime() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        return currentHour >= START_HOUR && currentHour < END_HOUR;
    }

    private static void runNaukriAutomation() {
        WebDriver driver = null;
        try {
            System.out.println("Starting Naukri automation at: " + new Date());
            
            // Setup ChromeDriver with headless mode
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36");
            
            driver = new ChromeDriver(options);
            
            // Navigate to Naukri.com with longer timeout
            driver.get("https://www.naukri.com/");
            System.out.println("Page loaded: " + driver.getTitle());
            
            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // Try multiple selectors for login button
            WebElement loginButton = null;
            
            // Try different selectors with timeout for each
            try {
                loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login_Layer")));
                System.out.println("Found login button by ID");
            } catch (Exception e) {
                System.out.println("Login button not found by ID, trying CSS selector...");
                try {
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='Jobseeker Login']")));
                    System.out.println("Found login button by CSS selector");
                } catch (Exception e2) {
                    System.out.println("Login button not found by CSS, trying XPath...");
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(text(),'Login') or contains(@title,'Login')]")));
                    System.out.println("Found login button by XPath");
                }
            }
            
            // Click login button
            loginButton.click();
            System.out.println("Login button clicked");
            
            // Wait for login form and fill credentials
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Enter your active Email ID / Username']")))
                .sendKeys(USERNAME);
            
            driver.findElement(By.xpath("//input[@placeholder='Enter your password']"))
                .sendKeys(PASSWORD);
            
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            System.out.println("Login form submitted");
            
            // Wait for login to complete
            Thread.sleep(5000);
            
            // Check if login was successful by looking for profile element
            try {
                WebElement profileElement = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.view-profile-wrapper a, .nI-gNb-profile, [title*='Profile']")));
                profileElement.click();
                System.out.println("Profile section accessed");
            } catch (Exception e) {
                System.out.println("Could not find profile element, trying direct resume upload...");
                // Try direct navigation to resume upload page
                driver.get("https://www.naukri.com/mnjuser/profile");
            }
            
            // Upload resume with multiple selector options
            WebElement uploadResume = null;
            try {
                uploadResume = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@id='attachCV']")));
                System.out.println("Found resume upload by ID");
            } catch (Exception e) {
                System.out.println("Resume input not found by ID, trying other selectors...");
                uploadResume = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[type='file'], #attachCV, [name='cv']")));
            }
            
            uploadResume.sendKeys(RESUME_PATH);
            System.out.println("Resume uploaded successfully at RINKU: " + new Date());
            
            // Wait for upload to complete and verify
            Thread.sleep(8000);
            
            // Check if upload was successful
            try {
                WebElement successMessage = driver.findElement(
                    By.xpath("//*[contains(text(),'Resume Updated') or contains(text(),'uploaded successfully')]"));
                System.out.println("Upload verification: " + successMessage.getText());
            } catch (Exception e) {
                System.out.println("No success message found, but resume might still be uploaded");
            }
            
        } catch (Exception e) {
            System.err.println("Error during automation: " + e.getMessage());
            e.printStackTrace();
            
            // Take screenshot for debugging (you'll need to add dependency for this)
            // File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            // System.out.println("Screenshot saved for debugging");
            
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("Browser closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing driver: " + e.getMessage());
                }
            }
        }
    }
}