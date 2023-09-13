package com.schroeder.photosync;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEncryptableProperties
@SpringBootApplication
public class PhotosyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotosyncApplication.class, args);
	}

}
