package com.hambugi.cullecting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class CullectingApplication {

	public static void main(String[] args) {
		// JVM시간을 한국시간으로 변경
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(CullectingApplication.class, args);
	}

}
