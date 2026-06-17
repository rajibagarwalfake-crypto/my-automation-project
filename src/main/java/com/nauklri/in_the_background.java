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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Navigate to login page
            driver.get("https://www.naukri.com/nlogin/login");
            System.out.println("Navigated to Naukri Login.");

            // ----- TRY MULTIPLE SELECTORS FOR USERNAME FIELD -----
            WebElement usernameField = null;
            try {
                usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            } catch (Exception e) {
                System.out.println("ID 'usernameField' not found, trying name attribute...");
                try {
                    usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
                } catch (Exception e2) {
                    System.out.println("Name 'username' not found, trying CSS selector...");
                    usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("input[type='text'][placeholder*='Email']")));
                }
            }
            usernameField.sendKeys(username);
            System.out.println("Username entered.");

            // ----- PASSWORD FIELD (try multiple) -----
            WebElement passwordField = null;
            try {
                passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("passwordField")));
            } catch (Exception e) {
                try {
                    passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
                } catch (Exception e2) {
                    passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("input[type='password']")));
                }
            }
            passwordField.sendKeys(password);
            System.out.println("Password entered.");

            // ----- LOGIN BUTTON -----
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit']")));
            loginButton.click();
            System.out.println("Login button clicked.");

            // Wait for login to complete and navigate to profile
            Thread.sleep(3000);
            driver.get("https://www.naukri.com/mnjuser/profile");
            System.out.println("Navigated to Profile Dashboard.");

            // ----- UPLOAD RESUME -----
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@type='file']")));
            fileInput.sendKeys(resumePath);
            System.out.println("Resume file path sent.");

            Thread.sleep(5000);
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
