package com.ragnax.sanbernardo.notificacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
@EnableAsync // 1. Habilita el soporte para @Async
@EnableScheduling
public class ComRagnaxSanbernardoNotificacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComRagnaxSanbernardoNotificacionApplication.class, args);
	}

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Configurando Thread Pool para procesos asíncronos (iText/Normalización)");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Hilos mínimos siempre activos
        executor.setMaxPoolSize(5);  // Hilos máximos si hay mucha carga
        executor.setQueueCapacity(500); // Cola de espera para archivos
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}
