package com.example.auto_demo.logic;

import com.example.auto_demo.config.AppConfig;
import com.example.auto_demo.util.DateUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class WebDriverLogic {

    public void ControlExistingBrowser(){

        try {

           System.setProperty("webdriver.chrome.driver", "C:\\Users\\zly\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe");

           //WebDriverManager.chromedriver().setup();
           ChromeOptions options = new ChromeOptions();
        // 连接到本地 9222 端口（与启动浏览器时的端口一致）s
            String port = new AppConfig().getBrowserPort();
            if(StringUtils.isEmpty(port)){
                port = "9223";
            }
            System.out.println( "端口：" + port);
            options.setExperimentalOption("debuggerAddress", "localhost:" + port);

            // 初始化 WebDriver（此时不会新建浏览器，而是连接到已打开的浏览器）
            WebDriver driver = new ChromeDriver(options);

            System.out.println("页面标题: " + driver.getTitle());

            for (String handle : driver.getWindowHandles()) {

                driver.switchTo().window(handle);
                String title = driver.getTitle();
                if(title.contains("低代码")){
                    break;
                }

            }
            Thread.sleep(10000);
            WebElement userAccount = driver.findElement(By.id("user_account"));
            userAccount.sendKeys("zhaojt");

            // 查找元素并操作
            WebElement userPwd = driver.findElement(By.id("account_pwd"));
            userPwd.sendKeys("12345678");

            Thread.sleep(3000);
            // WebElement loginButton = driver.findElement(By.xpath("//button[contains(@lay-filter,'login')]"));
            //driver.findElement(By.id("login"));
            WebElement loginButton = driver.findElement(By.xpath("//div[@id='login' and @style='']"));
            loginButton.click();

     /*   // 打开网页
        driver.get("http://paytest.1451cn.com:11122/orgine-poweradp-tools-web/page/orgine-poweradp-tools-web-login/index.html?network_type=public");
        driver.manage().window().maximize();
       // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // 查找元素并操作
        WebElement userAccount = driver.findElement(By.id("user_account"));
        userAccount.sendKeys("zhaojt");

        // 查找元素并操作
        WebElement userPwd = driver.findElement(By.id("account_pwd"));
        userPwd.sendKeys("12345678");

        Thread.sleep(3000);
        // WebElement loginButton = driver.findElement(By.xpath("//button[contains(@lay-filter,'login')]"));
        //driver.findElement(By.id("login"));
        WebElement loginButton = driver.findElement(By.xpath("//div[@id='login' and @style='']"));
        loginButton.click();*/

            //new Actions(driver).doubleClick(targetElement).perform();双击
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    public void uploadFile( String sessionId,String type ,String date) throws  Exception{
        try {
            WebDriver driver = locatorBrowser(sessionId);

            if("1".equals(type)){
                downloadFileByDay(sessionId,driver,type,date);
            }else if("2".equals(type)){
                downloadFileByMonth(sessionId,driver,type,date);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载医保对账文件异常:",e);
            throw e;
        }
    }

    public void downloadFileByDay(String sessionId, WebDriver driver,String type ,String date) throws  Exception{
        try {
            //创建等待对象（最长等待10秒，每500毫秒检查一次）
            WebDriverWait wait = new WebDriverWait(driver, 10000);

            //选择系统
            //log.trace();
            log.trace(sessionId + "开始获取机构辅助系统元素");
            WebElement sys = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'机构辅助系统')]")));

            String sys_class =  sys.getAttribute("class");

            log.info(sessionId + "获取机构辅助系统class:" + sys_class);

            if(!sys_class.contains("chosen")){
                sys.click();
                log.info(sessionId + "判断未选中机构辅助系统，已点击选中");
            }else{
                log.info(sessionId + "判断已选中机构辅助系统");
            }

            //选择定点医疗机构管理
            log.info(sessionId + "开始获取定点医疗机构管理元素");

            WebElement first_level_menu = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("span[contains(text(),'定点医疗机构管理')']/parent::*/parent::*/parent::li")));

            String first_level_menu_class =  first_level_menu.getAttribute("class");

            log.info(sessionId + "获取定点医疗机构管理菜单class:" + first_level_menu_class);

            if(!first_level_menu_class.contains("is_opened")){
                first_level_menu.click();;
                log.info(sessionId + "判断定点医疗机构管理菜单未展开，点击展开");
            } else {
                log.info(sessionId + "判断定点医疗机构管理菜单已展开");
            }

            //选择对账明细查询(按天)菜单
            log.info(sessionId + "开始获取对账明细查询(按天)菜单元素");
            //WebElement menu = driver.findElement(By.xpath("//span[contains(text(),'对账明细查询(按天)')]"));

            WebElement menu = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'对账明细查询(按天)')]"))
            );
            menu.click();
            log.info(sessionId + "已点击对账明细查询(按天)菜单");
            // 等待结果加载
            //Thread.sleep(3000);

            //选择时间
            log.info(sessionId + "开始获取时间选择控件元素");
            WebElement dateButton = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@class='ant-calendar-picker-input ant-input' and @placeholder='请选择日期']"))
            );
                    //driver.findElement(By.xpath("//input[@class='ant-calendar-picker-input ant-input' and @placeholder='请选择日期']") );
            dateButton.click();
            log.info(sessionId + "已点击时间选择控件");
           // Thread.sleep(1000);

            String dateValue = DateUtil.formatDate(date,"yyyy-MM-dd","yyyy年MM月dd日");

            WebElement dateCheckButton =  wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@role='gridcell' and @class='and-calendar-cell'] and title='"+dateValue+"'"))
            );
                    //driver.findElement(By.xpath("//td[@role='gridcell' and @class='and-calendar-cell'] and title='"+dateValue+"'") );
            dateCheckButton.click();
            log.info(sessionId + "已选择时间:" + dateValue);

            String insuTypes= new AppConfig().getInsuType();
            log.info(sessionId + "读取险种类型配置:" + insuTypes);
            if(StringUtils.isEmpty(insuTypes)){
               log.error(sessionId + "险种类型未配置,结束进程!");
               return;
            }

            String[] insuTypeArray = insuTypes.split(",");
            for (int i = 0; i < insuTypeArray.length; i++) {

                String insuType = insuTypeArray[i];
                log.info(sessionId + "开始获取险种类型控件元素");
                WebElement insuTypeButton = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("insutype"))
                );
                      //driver.findElement(By.id("insutype"));
                insuTypeButton.click();
                log.info(sessionId + "已点击险种类型控件");

                WebElement insuTypeValueButton = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@role='option' and @class='and-select-dropdown-menu-item'] and title='"+insuType+"'"))
                );
                        //driver.findElement(By.xpath("//li[@role='option' and @class='and-select-dropdown-menu-item'] and title='"+insuType+"'") );
                insuTypeValueButton.click();
                log.info(sessionId + "已选择险种类型:" + insuType);

                //导出全部
                log.info(sessionId + "开始获取导出全部按钮元素");
                WebElement expButton = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[title='全部导出'"))
                );
                        //driver.findElement(By.xpath("//span[title='全部导出'") );
                expButton.click();
                log.info(sessionId + "已导出结算日期为:" + dateValue+",险种类型为:" + insuType + "的对账明细");

                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载医保对账文件异常:",e);
            throw e;
        }
    }

    public void downloadFileByMonth(String sessionId, WebDriver driver,String type ,String date){
        try {
            //选择对账明细查询(按天)菜单
            log.info(sessionId + "开始获取对账明细查询(按月)菜单元素");
            WebElement menu = driver.findElement(By.xpath("//span[contains(text(),'对账明细查询(按月)')]"));
            menu.click();
            log.info(sessionId + "已点击对账明细查询(按月)菜单");
            // 等待结果加载
            Thread.sleep(3000);

            //选择时间
            log.info(sessionId + "开始获取时间选择控件元素");
            WebElement dateButton = driver.findElement(By.xpath("//input[@class='ant-calendar-picker-input ant-input' and @placeholder='请选择日期']") );
            dateButton.click();
            log.info(sessionId + "已点击时间选择控件");
            Thread.sleep(1000);

            String dateValue = DateUtil.formatDate(date,"yyyy-MM-dd","yyyy年MM月dd日");
            WebElement dateCheckButton = driver.findElement(By.xpath("//td[@role='gridcell' and @class='and-calendar-cell'] and title='"+dateValue+"'") );
            dateCheckButton.click();
            log.info(sessionId + "已选择时间:" + dateValue);

            String insuTypes= new AppConfig().getInsuType();
            log.info(sessionId + "读取险种类型配置:" + insuTypes);
            if(StringUtils.isEmpty(insuTypes)){
                log.error(sessionId + "险种类型未配置,结束进程!");
                return;
            }

            String[] insuTypeArray = insuTypes.split(",");
            for (int i = 0; i < insuTypeArray.length; i++) {

                String insuType = insuTypeArray[i];
                log.info(sessionId + "开始获取险种类型控件元素");
                WebElement insuTypeButton = driver.findElement(By.id("insutype"));
                insuTypeButton.click();
                log.info(sessionId + "已点击险种类型控件");

                WebElement insuTypeValueButton = driver.findElement(By.xpath("//li[@role='option' and @class='and-select-dropdown-menu-item'] and title='"+insuType+"'") );
                insuTypeValueButton.click();
                log.info(sessionId + "已选择险种类型:" + insuType);

                //导出全部
                log.info(sessionId + "开始获取导出全部按钮元素");
                WebElement expButton = driver.findElement(By.xpath("//span[title='全部导出'") );
                expButton.click();
                log.info(sessionId + "已导出结算日期为:" + dateValue+",险种类型为:" + insuType + "的对账明细");

                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载医保对账文件异常:",e);
        }
    }


    public WebDriver locatorBrowser(String sessionId){

        log.info(sessionId + "开始获取浏览器，自动下载浏览器驱动");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // 连接到本地 9222 端口（与启动浏览器时的端口一致）s
        String port = new AppConfig().getBrowserPort();
        log.info(sessionId + "读取浏览器启动端口的配置：" + port);

        if(StringUtils.isEmpty(port)){
            port = "9223";
            log.info(sessionId + "读取端口配置失败：采用默认端口:" + port);
        }
        options.setExperimentalOption("debuggerAddress", "localhost:" + port);

        // 初始化 WebDriver（此时不会新建浏览器，而是连接到已打开的浏览器）
        log.info(sessionId + "连接已打开的浏览器");
        WebDriver driver = new ChromeDriver(options);

        for (String handle : driver.getWindowHandles()) {

            driver.switchTo().window(handle);
            String title = driver.getTitle();
            if(title.contains("两定医疗保障信息平台")){
                log.info(sessionId + "通过页面标签定位到:" + title);
                break;
            }
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return driver;
    }

    public static void main(String[] args) {
        new WebDriverLogic().ControlExistingBrowser();
    }

}
