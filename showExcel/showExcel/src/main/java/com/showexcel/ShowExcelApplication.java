package com.showexcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import java.util.Arrays;

@SpringBootApplication
public class ShowExcelApplication {
    static String a = "";



    public static void main(String[] args) {

 
        SpringApplication.run(ShowExcelApplication.class, args);
        System.out.println("localhost:8080/showExcel");
    }

}
