// 

package com.uma.example.springuma.integration.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import com.uma.example.springuma.model.RepositoryPaciente;
import com.uma.example.springuma.model.RepositoryMedico;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PacienteRepositoryIT {

    @Autowired
    private RepositoryPaciente pacienteRepository;

    @Autowired
    private RepositoryMedico medicoRepository;

    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    public void setUp() {
        // Arrange
        paciente = new Paciente();
        paciente.setNombre("Paciente");
        paciente.setEdad(23);
        paciente.setCita("2023-10-01");
        paciente.setDni("87654321B");

        medico = new Medico();
        medico.setNombre("Medico");; 
        medico.setDni("12345678A");
        medico.setEspecialidad("Cirugía");
    }

    @Test
    @DisplayName("Check paciente is associated with medico")
    public void givenPaciente_whenAssociatedToNewMedico_thenMedicoIsAssociated() {
        // Arrange
        paciente.setMedico(medico);

        // Act
        Paciente savedPaciente = pacienteRepository.save(paciente);
        medicoRepository.save(medico);

        // Assert
        Optional<Paciente> retrievedPaciente = pacienteRepository.findById(savedPaciente.getId());
        assertTrue(retrievedPaciente.isPresent());
        assertEquals("87654321B", retrievedPaciente.get().getDni());
        assertEquals("12345678A", retrievedPaciente.get().getMedico().getDni());
    }

    @Test
    @DisplayName("Check paciente is associated with another medico")
    public void givenPaciente_whenAssociatedToAnotherMedico_thenMedicoIsAssociated() {
        // Arrange
        Medico medico2 = new Medico();
        medico2.setNombre("Medico2");
        medico2.setDni("18294765C");
        medico2.setEspecialidad("Pediatría");

        paciente.setMedico(medico);

        // Act
        medicoRepository.save(medico);
        medicoRepository.save(medico2);
        pacienteRepository.save(paciente);
        
        paciente.setMedico(medico2);
        Paciente savedPaciente = pacienteRepository.save(paciente);

        // Assert
        Optional<Paciente> retrievedPaciente = pacienteRepository.findById(savedPaciente.getId());
        assertTrue(retrievedPaciente.isPresent());
        assertEquals("87654321B", retrievedPaciente.get().getDni());
        assertEquals("18294765C", retrievedPaciente.get().getMedico().getDni());
    }

}
