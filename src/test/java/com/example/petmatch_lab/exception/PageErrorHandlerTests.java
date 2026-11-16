
package com.example.petmatch_lab.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PageErrorHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Test
    void shouldReturnNotFoundForMissingPet() throws Exception {
        mockMvc.perform(get("/pets/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenDeletingPetWithRequestHistory()
            throws Exception {

        Pet pet = petRepository.save(
                new Pet(null, "PruebaError", "Perro", 2, "Mascota de prueba")
        );

        supportRequestRepository.save(
                new SupportRequest(pet, SupportType.WALK, "Solicitud de prueba")
        );

        mockMvc.perform(post("/pets/" + pet.getId() + "/delete"))
                .andExpect(status().isConflict());
    }
}
