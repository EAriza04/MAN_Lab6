// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ImagenRepositoryIT {

    @Autowired
    private RepositoryImagen imagenRepository;

    @Autowired
    private RepositoryPaciente pacienteRepository;

    @Autowired
    private RepositoryMedico medicoRepository;

    @Test
    @DisplayName("Check imagen is persisted in the BBDD when created")
    void givenImagenEntity_whenSave_thenImagenIsPersisted() {
        // Arrange
        Medico medico = new Medico();
        medico.setNombre("Medico");
        medico.setDni("12345678A");
        medico.setEspecialidad("Cirugía");
        medico = medicoRepository.save(medico);

        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente");
        paciente.setEdad(23);
        paciente.setCita("2023-10-01");
        paciente.setDni("87654321B");
        paciente.setMedico(medico);
        paciente = pacienteRepository.save(paciente);


        Imagen imagen = new Imagen();
        imagen.setNombre("imagen.png");
        imagen.setFecha(Calendar.getInstance());
        imagen.setFile_content("imagen.png".getBytes());
        imagen.setPaciente(paciente);

        // Act
        Imagen savedImagen = imagenRepository.save(imagen);

        // Assert
        Optional<Imagen> retrievedImagen = imagenRepository.findById(savedImagen.getId());
        assertTrue(retrievedImagen.isPresent());
        assertEquals("imagen.png", retrievedImagen.get().getNombre());
        assertEquals(paciente.getId(), retrievedImagen.get().getPaciente().getId());
        assertArrayEquals("imagen.png".getBytes(), retrievedImagen.get().getFile_content());
    }
}
