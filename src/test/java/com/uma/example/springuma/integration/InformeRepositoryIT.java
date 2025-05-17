// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class InformeRepositoryIT {

    @Autowired
    private RepositoryInforme informeRepository;

    @Autowired
    private RepositoryPaciente pacienteRepository;

    @Autowired
    private RepositoryMedico medicoRepository;

    private Medico medico;

    private Paciente paciente;

    private Imagen imagen;

    private Informe informe;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setNombre("Medico");
        medico.setDni("12345678A");
        medico.setEspecialidad("Cirugía");
        medico = medicoRepository.save(medico);

        paciente = new Paciente();
        paciente.setNombre("Paciente");
        paciente.setEdad(23);
        paciente.setCita("2023-10-01");
        paciente.setDni("87654321B");
        paciente.setMedico(medico);
        paciente = pacienteRepository.save(paciente);

        imagen = new Imagen();
        imagen.setNombre("imagen.png");
        imagen.setFecha(Calendar.getInstance());
        imagen.setFile_content("imagen.png".getBytes());
        imagen.setPaciente(paciente);

        informe = new Informe();
        informe.setPrediccion("Predicción");
        informe.setContenido("Contenido");
        informe.setImagen(imagen);
    }

    @Test
    @DisplayName("Check informe is persisted in the BBDD when created")
    void givenInformeEntity_whenSave_thenInformeIsPersisted() {
        // Act
        Informe savedInforme = informeRepository.save(informe);

        // Assert
        Optional<Informe> retrievedInforme = informeRepository.findById(savedInforme.getId());
        assertTrue(retrievedInforme.isPresent());
        assertEquals("Predicción", retrievedInforme.get().getPrediccion());
        assertEquals("Contenido", retrievedInforme.get().getContenido());
        assertNotNull(retrievedInforme.get().getImagen());
    }

    @Test
    @DisplayName("Check informe is deleted in the BBDD when deleted")
    void givenInformeEntity_whenDelete_thenInformeIsDeleted() {
        // Act
        Informe savedInforme = informeRepository.save(informe);
        informeRepository.delete(savedInforme);

        // Assert
        Optional<Informe> retrievedInforme = informeRepository.findById(savedInforme.getId());
        assertFalse(retrievedInforme.isPresent());
    }

    
    
}
