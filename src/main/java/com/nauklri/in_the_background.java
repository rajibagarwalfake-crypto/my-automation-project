package com.nauklri;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

public class in_the_background {

    public static void main(String[] args) {
        String username = System.getenv("NAUKRI_USERNAME");
        String password = System.getenv("NAUKRI_PASSWORD");

        File resumeFile = new File("resume.docx");
        String resumePath = resumeFile.getAbsolutePath();

        System.out.println("=== Starting Naukri Profile Sync ===");
        System.out.println("Resume path: " + resumePath);

        if (!resumeFile.exists()) {
            System.err.println("ERROR: resume.docx not found in root!");
            System.exit(1);
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // STEP 1: Go to Homepage (not the direct login page)
            driver.get("https://www.naukri.com/");
            System.out.println("Navigated to Homepage. Title: " + driver.getTitle());

            Thread.sleep(3000); // Wait for page to load fully

            // STEP 2: Click the "Login" button on the homepage
            WebElement loginButton = null;
            try {
                loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login_Layer")));
            } catch (Exception e) {
                try {
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[title='Jobseeker Login']")));
                } catch (Exception e2) {
                    loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Login')]")));
                }
            }
            loginButton.click();
            System.out.println("Login button clicked. Looking for modal...");

            // STEP 3: Fill the Login Modal
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            usernameField.sendKeys(username);
            System.out.println("Username entered.");

            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("passwordField")));
            passwordField.sendKeys(password);
            System.out.println("Password entered.");

            WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
            submitButton.click();
            System.out.println("Login form submitted.");

            // STEP 4: Go to Profile page
            Thread.sleep(5000);
            driver.get("https://www.naukri.com/mnjuser/profile");
            System.out.println("Navigated to Profile Dashboard.");

            // STEP 5: Upload Resume
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
            fileInput.sendKeys(resumePath);
            System.out.println("Resume file path sent.");

            Thread.sleep(5000);
            System.out.println("✅ SUCCESS: Resume refreshed successfully!");

        } catch (Exception e) {
            System.err.println("AUTOMATION FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            driver.quit();
            System.out.println("Browser closed. Script finished.");
        }
    }
}
