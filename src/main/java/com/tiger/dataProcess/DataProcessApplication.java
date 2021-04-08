package com.tiger.dataProcess;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;

@SpringBootApplication
@ComponentScan(basePackages={"com.tiger.*"})
@EnableAutoConfiguration(exclude = { DruidDataSourceAutoConfigure.class,DataSourceAutoConfiguration.class})
public class DataProcessApplication {
	public static void main(String[] args) {
		SpringApplication.run(DataProcessApplication.class, args);
	}
}
