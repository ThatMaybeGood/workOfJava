//package com.example.auto_demo;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.time.Duration;
//import java.util.concurrent.TimeUnit;
//
//
//public class WebDriverTest {
//
//    public static void main(String[] args) throws InterruptedException {
//        // 1. 初始化浏览器驱动
//       // WebDriverManager.chromedriver().setup();
//        System.out.println("1");
////        System.setProperty("webdriver.chrome.driver", "C:\\Users\\zly\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe");
//        WebDriver driver = new ChromeDriver();
//
//        try {
//            // 窗口最大化 + 隐式等待
//       //     driver.manage().window().maximize();
//       //     driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
//
//            // 2. 访问百度首页
//            driver.get("https://www.baidu.com");
//
//
//
//            // 3. 显式等待搜索框并输入内容
//            WebDriverWait wait = new WebDriverWait(driver, 5000);
//           // WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chat-textarea")));
//
//
//            WebElement searchBox = driver.findElement(By.id("chat-textarea"));
//
//            searchBox.sendKeys("111");
//
//            // 4. 点击搜索按钮
//            WebElement searchBtn = driver.findElement(By.id("chat-submit-button"));
//            searchBtn.click();
//            Thread.sleep(2000);
//
//            // 5. 获取并打印第一条搜索结果
//            WebElement firstResult = wait.until(
//                    ExpectedConditions.visibilityOfElementLocated(
//                            By.xpath("//div[@id='content_left']/div[1]//h3/a")
//                    )
//            );
//         //   System.out.println("搜索结果标题：" + firstResult.getText());
//         //   System.out.println("结果链接：" + firstResult.getAttribute("href"));
//
//            // 6. 点击第一条结果并查看新页面标题
//            firstResult.click();
//            Thread.sleep(3000);
//           // System.out.println("当前页面标题：" + driver.getTitle());
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            // 7. 确保浏览器关闭，释放资源
//            Thread.sleep(2000);
//            driver.quit();
//          //  System.out.println("浏览器已关闭");
//        }
//
//    }
//}
