

package com.example.petmatch_lab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@SpringBootTest
class SupportRequestTransactionTests {

    @Autowired
    private SupportRequestService service;

    @Autowired
    private PetRepository petRepository;

    @MockitoSpyBean
    private SupportRequestRepository requestRepository;

    @Test
    void shouldRollbackCancellationWhenReplacementFails() {
        Pet pet = petRepository.save(
                new Pet(null, "RollbackTest", "Perro", 2, "Prueba")
        );

        SupportRequest oldRequest = requestRepository.save(
                new SupportRequest(
                        pet,
                        SupportType.WALK,
                        "Solicitud anterior"
                )
        );

        doThrow(new RuntimeException("Error simulado"))
                .when(requestRepository)
                .save(any(SupportRequest.class));

        SupportRequestForm form = new SupportRequestForm();
        form.setType(SupportType.COMPANY);
        form.setDescription("Nueva solicitud");

        assertThrows(
                RuntimeException.class,
                () -> service.replace(
                        pet.getId(),
                        oldRequest.getId(),
                        form
                )
        );

        SupportRequest savedRequest = requestRepository
                .findById(oldRequest.getId())
                .orElseThrow();

        assertEquals(RequestStatus.OPEN, savedRequest.getStatus());
    }
}
