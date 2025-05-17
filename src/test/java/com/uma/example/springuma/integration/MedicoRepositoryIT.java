// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.RepositoryMedico;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MedicoRepositoryIT {

    @Autowired
    private RepositoryMedico medicoRepository;

    private Medico medico;

    @BeforeEach
    void createMedicoEntity() {
        medico = new Medico();
        medico.setNombre("Medico");
        medico.setDni("122");
        medico.setEspecialidad("Cirugía");
    }

    @Test
    @DisplayName("Check medico is persited in the BBDD when created")
    void givenMedicoEntity_whenSaveUser_thenUserIsPersisted() {
        // act
        Medico savedMedico = medicoRepository.save(medico);

        // assert
        Optional<Medico> retrievedMedico = medicoRepository.findById(savedMedico.getId());
        assertTrue(retrievedMedico.isPresent());
        assertEquals("Medico", retrievedMedico.get().getNombre());
    }

    @Test
    @DisplayName("Check medico is changed in the BBDD when updated")
    void givenMedicoEntity_whenUpdateUser_thenUserIsUpdated() {
        // act
        Medico savedMedico = medicoRepository.save(medico);
        savedMedico.setNombre("Medico2");
        savedMedico.setDni("123");
        savedMedico.setEspecialidad("Cardiología");
        medicoRepository.save(savedMedico);

        // assert
        Optional<Medico> updatedMedico = medicoRepository.findById(savedMedico.getId());
        assertTrue(updatedMedico.isPresent());
        assertEquals("Medico2", updatedMedico.get().getNombre());
        assertEquals("123", updatedMedico.get().getDni());
        assertEquals("Cardiología", updatedMedico.get().getEspecialidad());
    }

    @Test
    @DisplayName("Check medico is retrieved in the BBDD when searched")
    void givenMedicoEntity_whenFindUser_thenUserIsFound(){
        // act
        Medico savedMedico = medicoRepository.save(medico);

        // assert
        Optional<Medico> retrievedMedico = medicoRepository.findById(savedMedico.getId());
        assertTrue(retrievedMedico.isPresent());
    }

    @Test
    @DisplayName("Check medico is deleted in the BBDD when deleted")
    void givenMedicoEntity_whenDeleteUser_thenUserIsDeleted() {
        // act
        Medico savedMedico = medicoRepository.save(medico);
        medicoRepository.delete(savedMedico);

        // assert
        Optional<Medico> deletedMedico = medicoRepository.findById(savedMedico.getId());
        assertFalse(deletedMedico.isPresent());
    }

}