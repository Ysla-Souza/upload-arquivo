package com.api.uploadarquivo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class UploadProperties {
	private String uploadDir;

	public String getUploadDir() {
		// TODO Auto-generated method stub
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

}