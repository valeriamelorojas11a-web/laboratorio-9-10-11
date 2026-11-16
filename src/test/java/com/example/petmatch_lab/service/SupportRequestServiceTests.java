
package com.example.petmatch_lab.service;

import java.util.Optional;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.model.SupportType;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportRequestServiceTests {

    @Mock
    private PetRepository petRepository;

    @Mock
    private SupportRequestRepository supportRequestRepository;

    @InjectMocks
    private SupportRequestService service;

    private Pet pet;

    @BeforeEach
    void setUp() {
        pet = new Pet(
                1L,
                "Luna",
                "Perro",
                2,
                "Mascota de prueba"
        );
    }

    private SupportRequestForm validForm() {
        SupportRequestForm form = new SupportRequestForm();
        form.setType(SupportType.WALK);
        form.setDescription("Necesita un paseo");
        return form;
    }

    @Test
    void shouldRejectSecondOpenRequest() {
        when(petRepository.findById(1L))
                .thenReturn(Optional.of(pet));

        when(supportRequestRepository.existsByPet_IdAndStatus(
                1L, RequestStatus.OPEN))
                .thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> service.create(1L, validForm())
        );

        verify(supportRequestRepository, never()).save(any());
    }

    @Test
    void shouldRejectRequestForMissingPet() {
        when(petRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.create(1L, validForm())
        );

        verify(supportRequestRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectCompletingRequestFromAnotherPet() {
        when(supportRequestRepository.findByIdAndPet_Id(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.complete(1L, 10L)
        );
    }

    @Test
    void shouldCancelOpenRequest() {
        SupportRequest request = new SupportRequest(
                pet,
                SupportType.WALK,
                "Necesita un paseo"
        );

        when(supportRequestRepository.findByIdAndPet_Id(10L, 1L))
                .thenReturn(Optional.of(request));

        service.cancel(1L, 10L);

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    void shouldRejectInvalidFormBeforeAccessingRepositories() {
        SupportRequestForm form = new SupportRequestForm();
        form.setType(null);
        form.setDescription("");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(1L, form)
        );

        verifyNoInteractions(petRepository);
        verifyNoInteractions(supportRequestRepository);
    }
}
