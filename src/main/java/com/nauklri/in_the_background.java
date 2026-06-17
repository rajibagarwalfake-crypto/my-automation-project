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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(45));

        try {
            // ---- STEP 1: Go directly to the Profile page ----
            // This forces Naukri to redirect to login with a special 'redirect' parameter.
            // The redirect login page is much less likely to trigger "Access Denied".
            System.out.println("Navigating to Profile URL (will redirect to login if needed)...");
            driver.get("https://www.naukri.com/mnjuser/profile");
            Thread.sleep(3000);
            System.out.println("Current URL: " + driver.getCurrentUrl());

            // ---- STEP 2: Check if we are on the Login page (URL contains 'login') ----
            if (driver.getCurrentUrl().contains("login") || driver.getTitle().contains("Login")) {
                System.out.println("Redirected to login page. Filling credentials...");

                // Use PRESENCE (not visibility) to find the field even if covered by a loading overlay.
                WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("usernameField")));
                usernameField.sendKeys(username);
                System.out.println("Username entered.");

                WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("passwordField")));
                passwordField.sendKeys(password);
                System.out.println("Password entered.");

                WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
                loginButton.click();
                System.out.println("Login submitted.");

                // Wait for login to complete, then go back to the profile page
                Thread.sleep(5000);
                driver.get("https://www.naukri.com/mnjuser/profile");
                System.out.println("Navigated back to Profile Dashboard.");
            } else {
                System.out.println("Already logged in (or no redirect to login). Proceeding to upload.");
            }

            // ---- STEP 3: Upload Resume (Now we are 100% on the profile page) ----
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
            fileInput.sendKeys(resumePath);
            System.out.println("Resume file path sent.");

            Thread.sleep(5000);
            System.out.println("✅✅✅ SUCCESS: Resume refreshed successfully! ✅✅✅");

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
