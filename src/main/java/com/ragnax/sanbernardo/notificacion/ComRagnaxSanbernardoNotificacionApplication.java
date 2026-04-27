package com.ragnax.sanbernardo.notificacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ComRagnaxSanbernardoNotificacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComRagnaxSanbernardoNotificacionApplication.class, args);
	}

}
