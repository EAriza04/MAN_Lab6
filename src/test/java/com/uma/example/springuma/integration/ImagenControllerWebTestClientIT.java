// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagenControllerWebTestClientIT {

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Paciente paciente;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000))
                .build();

        // Construimos un paciente ejemplo
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Paciente Test");
        paciente.setEdad(40);
        paciente.setDni("12345678A");
    }

    @Test
    @DisplayName("Subir imagen correctamente para un paciente")
    public void uploadImage_ShouldReturnSuccess() throws Exception {
        File uploadFile = new File("./src/test/resources/healthy.png");

        // Primero se crea el paciente
        client.post()
            .uri("/paciente")
            .body(Mono.just(paciente), Paciente.class)
            .exchange()
            .expectStatus().isCreated();

        // Se construye el cuerpo multipart para subir la imagen
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource(uploadFile));
        builder.part("paciente", paciente, MediaType.APPLICATION_JSON);

        // Subida de imagen y validación del JSON de respuesta
        client.post()
            .uri("/imagen")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.response")
            .value(resp -> assertEquals("file uploaded successfully : " + uploadFile.getName(), resp));
    }

    @Test
    @DisplayName("Realizar predicción aleatoria sobre imagen")
    public void predictImage_ShouldReturnPrediction() {
        File uploadFile = new File("./src/test/resources/no_healthty.png");

        // Primero se crea el paciente
        client.post()
            .uri("/paciente")
            .body(Mono.just(paciente), Paciente.class)
            .exchange()
            .expectStatus().isCreated();

        // Se construye el cuerpo multipart para subir la imagen
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource(uploadFile));
        builder.part("paciente", paciente, MediaType.APPLICATION_JSON);

        // Subida de imagen y validación del JSON de respuesta
        client.post()
            .uri("/imagen")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult();

        client.get()
            .uri("/imagen/predict/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status")
            .value(status -> assertTrue(
                status.equals("Cancer") || status.equals("Not cancer"),
                "El estado debe ser 'Cancer' o 'Not cancer'"
            ))
            .jsonPath("$.score")
            .value(score -> {
                assertInstanceOf(Number.class, score);
                double val = ((Number) score).doubleValue();
                assertTrue(val >= 0.0 && val <= 1.0, "Score debe estar entre 0 y 1");
            });
    }

}
