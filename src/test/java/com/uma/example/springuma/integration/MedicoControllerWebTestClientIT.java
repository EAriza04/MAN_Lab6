// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MedicoControllerWebTestClientIT {

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Medico medico;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000)).build();

        medico = new Medico();
        medico.setNombre("Medico");
        medico.setDni((int)(Math.random() * 99999999) + "Z");
        medico.setEspecialidad("Cirugía");
    }

    @Test
    @DisplayName("Crear un médico y obtenerlo correctamente")
    public void createMedicoPost_isObtainedWithGet() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico por dni
        FluxExchangeResult<Medico> result = client.get().uri("/medico/dni/" + medico.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class);

        Medico medicoObtained = result.getResponseBody().blockFirst();
        assertNotNull(medicoObtained);
        assertEquals(medico.getDni(), medicoObtained.getDni());
        assertEquals(medico.getNombre(), medicoObtained.getNombre());
        assertEquals(medico.getEspecialidad(), medicoObtained.getEspecialidad());
    }

    @Test
    @DisplayName("Actualizar un médico correctamente")
    public void updateMedicoPut_isUpdatedCorrectly() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico para conseguir el id
        Medico medicoObtained = client.get().uri("/medico/dni/" + medico.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();

        assertNotNull(medicoObtained);

        // Modificar datos
        medicoObtained.setNombre("Dr. Editado");
        medicoObtained.setEspecialidad("Cardiología");

        // Actualizar
        client.put().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medicoObtained), Medico.class)
                .exchange()
                .expectStatus().isNoContent();

        // Comprobar actualización
        Medico medicoUpdated = client.get().uri("/medico/" + medicoObtained.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();

        assertNotNull(medicoUpdated);
        assertEquals("Dr. Editado", medicoUpdated.getNombre());
        assertEquals("Cardiología", medicoUpdated.getEspecialidad());
        assertEquals(medicoObtained.getDni(), medicoUpdated.getDni());
    }

    @Test
    @DisplayName("Eliminar un médico correctamente")
    public void deleteMedicoDelete_isDeletedCorrectly() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico para conseguir el id
        Medico medicoObtained = client.get().uri("/medico/dni/" + medico.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();

        assertNotNull(medicoObtained);

        // Eliminar médico
        client.delete().uri("/medico/" + medicoObtained.getId())
                .exchange()
                .expectStatus().isOk();

        // Comprobar que ya no existe
        client.get().uri("/medico/" + medicoObtained.getId())
                .exchange()
                .expectStatus().is5xxServerError();
    }

}
