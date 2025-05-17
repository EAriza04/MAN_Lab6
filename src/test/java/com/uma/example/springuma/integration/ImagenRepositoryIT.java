// Eduardo Ariza Abad y Enrique Ibañez Rico

package com.uma.example.springuma.integration;

import com.uma.example.springuma.model.RepositoryImagen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ImagenRepositoryIT {

    @Autowired
    private RepositoryImagen imagenRepository;
    
}
