// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InformeControllerWebTestClientIT {

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Medico medico;
    private Paciente paciente;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000)).build();

        medico = new Medico();
        medico.setNombre("MedicoTest");
        medico.setDni((int)(Math.random() * 99999999) + "A");
        medico.setEspecialidad("Radiología");

        paciente = new Paciente();
        paciente.setNombre("PacienteTest");
        paciente.setEdad(40);
        paciente.setCita("2024-06-01");
        paciente.setDni((int)(Math.random() * 99999999) + "Z");
    }

    @Test
    @DisplayName("Crear un informe y obtenerlo correctamente")
    public void createInformePost_isObtainedWithGet() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico
        Medico medicoCreado = client.get().uri("/medico/dni/" + medico.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();
        assertNotNull(medicoCreado);

        // Crear paciente
        paciente.setMedico(medicoCreado);
        client.post().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener paciente
        Paciente pacienteCreado = client.get().uri("/paciente/medico/" + medicoCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody()
                .filter(p -> paciente.getDni().equals(p.getDni()))
                .blockFirst();
        assertNotNull(pacienteCreado);

        // Crear imagen
        File uploadFile = new File("./src/test/resources/healthy.png");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource(uploadFile));
        builder.part("paciente", pacienteCreado, MediaType.APPLICATION_JSON);

        client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        // Obtener imagen
        Imagen imagenCreada = client.get().uri("/imagen/paciente/" + pacienteCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Imagen.class)
                .getResponseBody()
                .filter(i -> "healthy.png".equals(i.getNombre()))
                .blockFirst();
        assertNotNull(imagenCreada);

        // Crear informe
        Informe informeTest = new Informe();
        informeTest.setImagen(imagenCreada);

        client.post().uri("/informe")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(informeTest), Informe.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener informe
        FluxExchangeResult<Informe> result = client.get().uri("/informe/imagen/" + imagenCreada.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Informe.class);

        Informe informeObtained = result.getResponseBody().blockFirst();

        assertEquals(informeTest.getImagen().getId(), informeObtained.getImagen().getId());
    }

    @Test
    @DisplayName("Crear un informe y eliminarlo correctamente")
    public void createInformePost_DeleteitCorrectly() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico
        Medico medicoCreado = client.get().uri("/medico/dni/" + medico.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();
        assertNotNull(medicoCreado);

        // Crear paciente
        paciente.setMedico(medicoCreado);
        client.post().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener paciente
        Paciente pacienteCreado = client.get().uri("/paciente/medico/" + medicoCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody()
                .filter(p -> paciente.getDni().equals(p.getDni()))
                .blockFirst();
        assertNotNull(pacienteCreado);

        // Crear imagen
        File uploadFile = new File("./src/test/resources/healthy.png");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource(uploadFile));
        builder.part("paciente", pacienteCreado, MediaType.APPLICATION_JSON);

        client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        // Obtener imagen
        Imagen imagenCreada = client.get().uri("/imagen/paciente/" + pacienteCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Imagen.class)
                .getResponseBody()
                .filter(i -> "healthy.png".equals(i.getNombre()))
                .blockFirst();
        assertNotNull(imagenCreada);

        // Crear informe
        Informe informeTest = new Informe();
        informeTest.setImagen(imagenCreada);

        client.post().uri("/informe")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(informeTest), Informe.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener informe
        FluxExchangeResult<Informe> result = client.get().uri("/informe/imagen/" + imagenCreada.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Informe.class);

        Informe informeObtained = result.getResponseBody().blockFirst();

        // Eliminar informe
        client.delete().uri("/informe/" + informeObtained.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Comprobar que ya no existe
        client.get().uri("/informe/" + informeObtained.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Informe.class)
                .isEqualTo(null);
    }
}
