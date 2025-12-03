package br.com.agrogame.agrogame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class GcpStorageConfig {

	@Bean
	public Storage storage() {
		return StorageOptions.getDefaultInstance().getService();
	}
}
