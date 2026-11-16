
package com.example.petmatch_lab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;

@SpringBootTest
@Transactional
class SupportRequestRepositoryTests {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Test
    void shouldFindRequestsByPet() {
        Pet pet = petRepository.save(
            new Pet(null, "PruebaRepo", "Perro", 2, "Mascota de prueba")
        );

        supportRequestRepository.save(
            new SupportRequest(pet, SupportType.WALK, "Solicitud uno")
        );

        supportRequestRepository.save(
            new SupportRequest(pet, SupportType.COMPANY, "Solicitud dos")
        );

        List<SupportRequest> requests =
            supportRequestRepository.findByPet_IdOrderByIdDesc(pet.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void shouldDetectRequestHistoryForPet() {
        Pet pet = petRepository.save(
            new Pet(null, "PruebaExiste", "Gato", 1, "Mascota de prueba")
        );

        supportRequestRepository.save(
            new SupportRequest(pet, SupportType.FEEDING, "Solicitud de comida")
        );

        assertTrue(
            supportRequestRepository.existsByPet_Id(pet.getId())
        );
    }

    @Test
    void shouldDetectOpenRequestForPet() {
        Pet pet = petRepository.save(
            new Pet(null, "PruebaAbierta", "Perro", 3, "Mascota de prueba")
        );

        supportRequestRepository.save(
            new SupportRequest(pet, SupportType.WALK, "Solicitud abierta")
        );

        assertTrue(
            supportRequestRepository.existsByPet_IdAndStatus(
                pet.getId(), RequestStatus.OPEN
            )
        );
    }
}
