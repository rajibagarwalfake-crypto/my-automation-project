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
        // 1. Fetch credentials securely from GitHub environment variables
        String username = System.getenv("NAUKRI_USERNAME");
        String password = System.getenv("NAUKRI_PASSWORD");
        
        // 2. Locate the resume file dynamically in the GitHub workspace root
        File resumeFile = new File("resume.docx");
        String resumePath = resumeFile.getAbsolutePath();

        System.out.println("=== Starting Naukri Profile Sync ===");
        System.out.println("Checking resume file path: " + resumePath);
        
        if (!resumeFile.exists()) {
            System.err.println("ERROR: resume.docx not found in root directory!");
            System.exit(1);
        }

        // 3. Set up Chrome options for Headless Execution (Crucial for GitHub Actions)
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Runs completely in background without a screen
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // 4. Navigate to Naukri Login
            driver.get("https://www.naukri.com/nlogin/login");
            System.out.println("Navigated to Naukri Login Page.");

            // 5. Input Username
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            usernameField.sendKeys(username);

            // 6. Input Password
            WebElement passwordField = driver.findElement(By.id("passwordField"));
            passwordField.sendKeys(password);

            // 7. Click Login
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
            loginButton.click();
            System.out.println("Login form submitted.");

            // 8. Go directly to View Profile page
            driver.get("https://www.naukri.com/mnjuser/profile");
            System.out.println("Navigated to Profile Dashboard.");

            // 9. Find the invisible file upload input element and send the path
            // (Naukri accepts file paths directly via standard <input type="file"> interaction)
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
            fileInput.sendKeys(resumePath);
            System.out.println("Resume file path sent successfully.");

            // 10. Wait briefly for the UI to register the successful upload message toast
            Thread.sleep(5000); 
            System.out.println("SUCCESS: Resume refreshed successfully at the top of recruiter queues!");

        } catch (Exception e) {
            System.err.println("AUTOMATION FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Tells GitHub Action that the job failed so you can get a red cross notification
        } finally {
            // Close down the browser engine safely
            driver.quit();
            System.out.println("=== Browser Closed. Automation finished ===");
        }
    }
}
