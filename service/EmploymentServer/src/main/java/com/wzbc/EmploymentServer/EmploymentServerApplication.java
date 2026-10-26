package com.wzbc.EmploymentServer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wzbc.EmploymentServer.Mapper")
public class EmploymentServerApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(EmploymentServerApplication.class, args);
    }
}
