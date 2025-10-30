package com.showexcel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import java.util.Arrays;

@SpringBootApplication
@MapperScan("com.showexcel.mapper")
public class ShowExcelApplication {

    public static void main(String[] args) {


        SpringApplication.run(ShowExcelApplication.class, args);
        System.out.println("http://localhost:8080");
    }

}
