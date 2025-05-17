package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PacienteControllerWebTestClientIT {

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Medico medico1;
    private Medico medico2;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMillis(30000)).build();

        medico1 = new Medico();
        medico1.setNombre("Medico1");
        medico1.setDni((int)(Math.random() * 99999999) + "A");
        medico1.setEspecialidad("Pediatría");

        medico2 = new Medico();
        medico2.setNombre("Medico2");
        medico2.setDni((int)(Math.random() * 99999999) + "B");
        medico2.setEspecialidad("Cardiología");
    }

    @Test
    @DisplayName("Asociar paciente a médico y editar paciente")
    public void associateAndEditPacienteToMedico() {
        // Crear médico
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico1), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médico creado
        Medico medicoCreado = client.get().uri("/medico/dni/" + medico1.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();
        assertNotNull(medicoCreado);

        // Paciente
        Paciente pacienteTest = new Paciente();
        pacienteTest.setNombre("Paciente");
        pacienteTest.setEdad(30);
        pacienteTest.setCita("2024-06-01");
        pacienteTest.setDni((int)(Math.random() * 99999999) + "Z");
        pacienteTest.setMedico(medicoCreado);

        // Crear paciente
        client.post().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(pacienteTest), Paciente.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener paciente creado
        Paciente pacienteObtained = client.get().uri("/paciente/medico/" + medicoCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody()
                .filter(p -> pacienteTest.getDni().equals(p.getDni()))
                .blockFirst();
        assertNotNull(pacienteObtained);

        // Editar paciente
        pacienteObtained.setNombre("Paciente Editado");
        pacienteObtained.setEdad(40);

        client.put().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(pacienteObtained), Paciente.class)
                .exchange()
                .expectStatus().isNoContent();

        // Comprobar edición
        Paciente pacienteEditado = client.get().uri("/paciente/" + pacienteObtained.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody().blockFirst();

        assertEquals("Paciente Editado", pacienteEditado.getNombre());
        assertEquals(40, pacienteEditado.getEdad());
    }

    @Test
    @DisplayName("Cambiar médico de un paciente y detectar el cambio")
    public void changeMedicoOfPaciente_andDetectChange() {
        // Crear médicos
        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico1), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico2), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener médicos creados
        Medico m1 = client.get().uri("/medico/dni/" + medico1.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();
        Medico m2 = client.get().uri("/medico/dni/" + medico2.getDni())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Medico.class)
                .getResponseBody().blockFirst();

        // Paciente
        Paciente pacienteTest = new Paciente();
        pacienteTest.setNombre("Paciente");
        pacienteTest.setEdad(30);
        pacienteTest.setCita("2024-06-01");
        pacienteTest.setDni((int)(Math.random() * 99999999) + "Z");
        pacienteTest.setMedico(m1);

        // Crear paciente
        client.post().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(pacienteTest), Paciente.class)
                .exchange()
                .expectStatus().isCreated();

        // Obtener pacientes creados
        Paciente pacienteCreado = client.get().uri("/paciente/medico/" + m1.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody()
                .filter(p -> pacienteTest.getDni().equals(p.getDni()))
                .blockFirst();
        assertNotNull(pacienteCreado);

        // Cambiar médico a m2
        pacienteCreado.setMedico(m2);

        client.put().uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(pacienteCreado), Paciente.class)
                .exchange()
                .expectStatus().isNoContent();

        // Comprobar cambio de médico
        Paciente pacienteActualizado = client.get().uri("/paciente/" + pacienteCreado.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Paciente.class)
                .getResponseBody().blockFirst();

        assertEquals(m2.getDni(), pacienteActualizado.getMedico().getDni());
    }
    
}
