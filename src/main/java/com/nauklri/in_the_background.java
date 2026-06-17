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

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {
            driver.get("https://www.naukri.com/nlogin/login");
            System.out.println("Page loaded. Title: " + driver.getTitle());

            // ----- FIND USERNAME FIELD USING MULTIPLE STRATEGIES -----
            WebElement usernameField = null;
            String[] userSelectors = {
                "//input[contains(@placeholder, 'Email')]",
                "//input[contains(@placeholder, 'Username')]",
                "//input[@name='username']",
                "//input[@id='usernameField']",
                "//input[@type='text'][@autocomplete='username']",
                "//input[@type='email']"
            };
            for (String selector : userSelectors) {
                try {
                    usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selector)));
                    System.out.println("Found username with: " + selector);
                    break;
                } catch (Exception e) {
                    // continue to next selector
                }
            }

            if (usernameField == null) {
                throw new Exception("Could not find username field.");
            }
            usernameField.sendKeys(username);
            System.out.println("Username entered.");

            // ----- FIND PASSWORD FIELD -----
            WebElement passwordField = null;
            String[] passSelectors = {
                "//input[contains(@placeholder, 'Password')]",
                "//input[@name='password']",
                "//input[@id='passwordField']",
                "//input[@type='password']"
            };
            for (String selector : passSelectors) {
                try {
                    passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selector)));
                    System.out.println("Found password with: " + selector);
                    break;
                } catch (Exception e) {
                    // continue
                }
            }

            if (passwordField == null) {
                throw new Exception("Could not find password field.");
            }
            passwordField.sendKeys(password);
            System.out.println("Password entered.");

            // ----- LOGIN BUTTON -----
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit']")));
            loginButton.click();
            System.out.println("Login button clicked.");

            // Wait for login and navigate to profile
            Thread.sleep(5000);
            driver.get("https://www.naukri.com/mnjuser/profile");
            System.out.println("Navigated to Profile Dashboard.");

            // ----- UPLOAD RESUME -----
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@type='file']")));
            fileInput.sendKeys(resumePath);
            System.out.println("Resume file path sent.");

            Thread.sleep(6000);
            System.out.println("SUCCESS: Resume refreshed!");

        } catch (Exception e) {
            System.err.println("AUTOMATION FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            driver.quit();
            System.out.println("Browser closed.");
        }
    }
}
