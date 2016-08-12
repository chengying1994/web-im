package com.easemob.webim.webim_test;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.google.common.base.Preconditions;

public class WebIMTestBase {
	public static String PROPERTY_BASE_URL = "BASE_URL";
	public static String PROPERTY_INTERNAL_BASE_URL = "INTERNAL_BASE_URL";
	public static String PROPERTY_USER_NAME = "USER_NAME";
	public static String PROPERTY_INTERNAL_USER_NAME = "INTERNAL_USER_NAME";
	public static String PROPERTY_USER_PASSWORD = "USER_PASSWORD";
	public static String PROPERTY_INTERNAL_USER_PASSWORD = "INTERNAL_USER_PASSWORD";

	private static final Logger logger = LoggerFactory.getLogger(WebIMTestBase.class);

	protected WebDriver driver;
	protected String baseUrl;
	protected String username;
	protected String password;
	protected String screenshotPath = "target";
	protected String screenshotSuffix = "png";

	protected boolean isGetBaseUrl = true;

	public void init() {
		if (StringUtils.isNotBlank(System.getProperty(PROPERTY_BASE_URL))) {
			baseUrl = System.getProperty(PROPERTY_BASE_URL);
		} else if (StringUtils.isNotBlank(System.getProperty(PROPERTY_INTERNAL_BASE_URL))) {
			String path = System.getProperty(PROPERTY_INTERNAL_BASE_URL);
			//find local index.html
			if (!path.contains("index.html")) {
				File file = new File(path);
				if (file.isDirectory()) {

					baseUrl = "file://" + file.getParentFile().getAbsolutePath() + System.getProperty("file.separator") + "index.html";
				}
			} else {
				baseUrl = System.getProperty(PROPERTY_INTERNAL_BASE_URL);
			}
		}
		logger.info("Initial base url: {}", baseUrl);
		if (StringUtils.isNotBlank(System.getProperty(PROPERTY_USER_NAME))) {
			username = System.getProperty(PROPERTY_USER_NAME);
		} else if (StringUtils.isNotBlank(System.getProperty(PROPERTY_INTERNAL_USER_NAME))) {
			username = System.getProperty(PROPERTY_INTERNAL_USER_NAME);
		}
		logger.info("Initial username: {}", username);
		if (StringUtils.isNotBlank(System.getProperty(PROPERTY_USER_PASSWORD))) {
			password = System.getProperty(PROPERTY_USER_PASSWORD);
		} else if (StringUtils.isNotBlank(System.getProperty(PROPERTY_INTERNAL_USER_PASSWORD))) {
			password = System.getProperty(PROPERTY_INTERNAL_USER_PASSWORD);
		}
		logger.info("Initial password: {}", password);
	}

	public void login(WebDriver driver, String username, String password, String path, boolean isGetBaseUrl) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		Preconditions.checkArgument(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password),
				"username or password was missing!");
		WebElement we = checkLogin(driver);
		if (null != we && we.isDisplayed()) {
			return;
		}
		if (isGetBaseUrl) {
			driver.get(baseUrl);
		}
		driver.manage().window().maximize();
		sleep(5);
		logger.info("find username box and input username: {}", username);
		String xpath = "//article[@id='demo']/div/div/div[2]/input[1]";
		WebElement usernameInput = findElementByXpath(driver, xpath);
		if (null == usernameInput) {
			screenshot(driver, getPath(path));
		}
		Assert.assertNotNull(usernameInput);
		usernameInput.clear();
		usernameInput.sendKeys(username);

		logger.info("find password box and input password: {}", password);
		xpath = "//article[@id='demo']/div/div/div[2]/input[2]";
		WebElement passwordInput = findElementByXpath(driver, xpath);
		if (null == passwordInput) {
			screenshot(driver, getPath(path));
		}
		Assert.assertNotNull(passwordInput);
		passwordInput.clear();
		passwordInput.sendKeys(password);

		logger.info("click login button");
		xpath = "//article[@id='demo']/div/div/div[2]/button";
		WebElement login = findElementByXpath(driver, xpath);
		if (null == login) {
			screenshot(driver, getPath(path));
		}
		Assert.assertNotNull(login);
		login.click();
		sleep(10);

		logger.info("check if login webim successfully");
		// xpath = "//a[@id='accordion1']";
		// WebElement ele = findElementByXpath(driver, xpath);
		WebElement ele = checkLogin(driver);
		if (null == ele) {
			screenshot(driver, getPath(path));
		}
		Assert.assertNotNull(ele);
	}

	public WebElement checkLogin(WebDriver driver) {
		String xpath = "//article[@id='demo']/div/div/div[4]/div[1]/div[1]/img";
		WebElement ele = null;
		try {
			ele = findElementByXpath(driver, xpath);
		} catch (Exception e) {
			logger.error("Failed to check login page", e);
			ele = null;
		}
		return ele;
	}

	public WebElement findSpecialFriend(WebDriver driver, String username, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		Preconditions.checkArgument(StringUtils.isNotBlank(username), "friend name was missing!");
		String xpath = "//div[@class='webim-leftbar']/div[@id='friends']/i[1]";
		WebElement ele = findElement(driver, xpath, path);
		if (ele.getAttribute("class").equals("webim-leftbar-icon font small")) {
			ele.click();
		}
		sleep(3);
		xpath = "//div[@class='webim-contact-wrapper']/div[1]/div[@id='"+username+"']";
		ele = findElement(driver, xpath, path);
		if (StringUtils.isNotBlank(ele.getAttribute("class"))) {
			ele.click();
		}
		return ele;
	}

	public void checkChatMsg(WebDriver driver, String username1, String username2, String msg, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		Preconditions.checkArgument(StringUtils.isNotBlank(username1) && StringUtils.isNotBlank(username2),
				"username1 or username2 was missing");
		Preconditions.checkArgument(StringUtils.isNotBlank(msg), "message was missing");
		WebElement wet = checkLogin(driver);
		Assert.assertTrue(null != wet && wet.isDisplayed(), "check login web page");
		String xpath = "//div[@class='webim-chatwindow ']/div[@id='wrapper"+username2+"']";
		WebElement ele = findElement(driver, xpath, path);
		try {
			List<WebElement> eles = ele.findElements(By.xpath("//pre"));
			for (WebElement we : eles) {
				if (we.getText().contains(msg)) {
					logger.info("find message: {}", msg);
					return;
				}
			}
			Assert.assertTrue(false,
					"find chat log: user1: " + username1 + ", user2: " + username2 + ", message: " + msg);
		} catch (Exception e) {
			logger.error("Failed to find chat log: user1: {}, user2: {}, message: {}", username1, username2, msg, e);
			Assert.assertTrue(false,
					"find chat log: user1: " + username1 + ", user2: " + username2 + ", message: " + msg);
		}
	}

	public void sendFile(WebDriver driver, String filePath, String data_type, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		Preconditions.checkArgument(StringUtils.isNotBlank(filePath) && StringUtils.isNotBlank(data_type),
				"file path or data type was missing");
		logger.info("find file input");
		String xpath = "//input[@id='uploadShim']";
		WebElement ele = findElement(driver, xpath, path);
		sleep(1);
		logger.info("reset file input property");
		JavascriptExecutor jse = (JavascriptExecutor) driver;
//		jse.executeScript("$('#uploadShim').show(); $('#uploadShim').attr('data-type', '" + data_type + "');");
		jse.executeScript("var fileele = document.getElementById('uploadShim'); fileele.style.display='block';fileele.setAttribute('data-type','" + data_type + "');");
		sleep(3);
		File file = new File(filePath);
		String str = null;
		if (file.exists()) {
			logger.info("find resource file: {}", file.getAbsolutePath());
			str = file.getAbsolutePath();
		}
		Assert.assertNotNull(str, "resource file path");
		str = "C:/Users/Public/test_img.png";
		ele.sendKeys(str); 
		sleep(1);
		logger.info("set back file input property");
		sleep(3);
		jse.executeScript("var fileele = document.getElementById('uploadShim'); fileele.style.display='none'");
	}

	public void logout(WebDriver driver, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		logger.info("click logout button");
		String xpath = "//div[@class='webim-leftbar']/div[6]/i";
		WebElement ele = findElement(driver, xpath, path);
		ele.click();
		sleep(1);
		xpath = "//div[@class='webim-leftbar']/div[6]/ul[@class='webim-operations']/li[3]";
		ele = findElement(driver, xpath, path);
		ele.click();
		sleep(3);
		logger.info("find login button");
		xpath = "//div[@class='webim-sign']/p/i";
		findElement(driver, xpath, path);
	}

	public String getPath(String path) {
		return path + "_" + System.currentTimeMillis() + "." + screenshotSuffix;
	}

	@SuppressWarnings("static-access")
	public void sleep(int seconds) {
		logger.info("Start to sleep {} seconds...", seconds);
		try {
			Thread.currentThread().sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			logger.error("Failed to sleep {} seconds", seconds);
		}
	}

	public void screenshot(WebDriver driver, String path) {
		Preconditions.checkArgument(StringUtils.isNotBlank(path), "screenshot file path was missing!");
		try {
			TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
			File srcFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(srcFile, new File(path));
		} catch (Exception e) {
			logger.error("Failed to get screenshot: path[{}]", path, e);
		}
	}

	public WebElement findElementByXpath(WebDriver driver, String xpath) {
		WebElement element = null;
		try {
			element = driver.findElement(By.xpath(xpath));
		} catch (Exception e) {
			logger.error("Failed to find element: xpath[{}]", xpath, e);
			return null;
		}
		return element;
	}

	public WebElement findElement(WebDriver driver, String xpath, String path) {
		WebElement element = findElementByXpath(driver, xpath);
		if (null == element) {
			logger.error("Find element is null: xpath[{}]", xpath);
			screenshot(driver, getPath(path));
		}
		Assert.assertNotNull(element, "Find element with xpath[" + xpath + "]");
		return element;
	}

	public String getRandomStr(int count) {
		return RandomStringUtils.randomAlphanumeric(count).toLowerCase();
	}
	
	public WebElement findSpecialGroup(WebDriver driver, String groupId, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		String xpath = "//div[@class='webim-chat']/div[@class='webim-leftbar']/div[@id='groups']/i";
		WebElement ele = findElement(driver, xpath, path);
		if (ele.getAttribute("class").equals("webim-leftbar-icon font small")) {
			ele.click();
			sleep(1);
			logger.info("test");
		}
		if (StringUtils.isNotBlank(groupId)) {
			logger.info("select group: {}", groupId);
			xpath = "//div[@class='webim-chat']/div[@class='webim-contact-wrapper']/div[2]/div[@id='" + groupId + "']";
			logger.info("here1");
		} 
		else {
			logger.info("select first group");
			xpath = "//div[@class='webim-chat']/div[@class='webim-contact-wrapper']/div[2]/div[1]";
			logger.info("here2");
		}
		ele = findElement(driver, xpath, path);
		if (StringUtils.isNotBlank(ele.getAttribute("class"))) {
			ele.click();
			sleep(1);
			logger.info("test1");
		}
		return ele;
	}
	
	public WebElement findSpecialChatroom(WebDriver driver, String chatroomId, String path) {
		Preconditions.checkArgument(null != driver, "webdriver was missing");
		String xpath = "//div[@class='webim-chat']/div[@class='webim-leftbar']/div[@id='chatrooms']/i";
		WebElement ele = findElement(driver, xpath, path);
		if (ele.getAttribute("class").equals("webim-leftbar-icon font small")) {
			ele.click();
			sleep(1);
		}
		if (StringUtils.isNotBlank(chatroomId)) {
			logger.info("select chatroom: {}", chatroomId);
			xpath = "//div[@class='webim-contact-wrapper']/div[3]/div[@id='" + chatroomId + "']";
		} else {
			logger.info("select first chatroom");
			xpath = "//div[@class='webim-contact-wrapper']/div[3]/div[1]";
		}
		ele = findElement(driver, xpath, path);
		if (StringUtils.isNotBlank(ele.getAttribute("class"))) {
			ele.click();
			sleep(5);
		}
		return ele;
	}
	
	public String getScreenshotPath(String name) {
		return screenshotPath + "/" + name;
	}
}
