package com.example.petmatch_lab.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.dto.SupportRequestForm;
import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.exception.ResourceNotFoundException;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.RequestStatus;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@Service
public class SupportRequestService {

    private final PetRepository petRepository;
    private final SupportRequestRepository supportRequestRepository;

    public SupportRequestService(
            PetRepository petRepository,
            SupportRequestRepository supportRequestRepository) {
        this.petRepository = petRepository;
        this.supportRequestRepository = supportRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<SupportRequest> findAllForPet(long petId) {
        findPet(petId);
        return supportRequestRepository.findByPet_IdOrderByIdDesc(petId);
    }

    @Transactional(readOnly = true)
    public SupportRequest findOneForPet(long petId, long requestId) {
        return findRequest(petId, requestId);
    }

    @Transactional
    public SupportRequest create(long petId, SupportRequestForm form) {
        validateForm(form);

        Pet pet = findPet(petId);

        if (supportRequestRepository.existsByPet_IdAndStatus(
                petId, RequestStatus.OPEN)) {
            throw new BusinessRuleException(
                    "La mascota ya tiene una solicitud abierta.");
        }

        SupportRequest request = new SupportRequest(
                pet,
                form.getType(),
                form.getDescription().strip()
        );

        return supportRequestRepository.save(request);
    }

    @Transactional
    public void complete(long petId, long requestId) {
        SupportRequest request = findRequest(petId, requestId);
        request.complete();
    }

    @Transactional
    public void cancel(long petId, long requestId) {
        SupportRequest request = findRequest(petId, requestId);
        request.cancel();
    }

    @Transactional
    public SupportRequest replace(
            long petId,
            long requestId,
            SupportRequestForm form) {

        validateForm(form);

        SupportRequest oldRequest = findRequest(petId, requestId);

        oldRequest.cancel();

        SupportRequest newRequest = new SupportRequest(
                oldRequest.getPet(),
                form.getType(),
                form.getDescription().strip()
        );

        return supportRequestRepository.save(newRequest);
    }

    private void validateForm(SupportRequestForm form) {
        if (form == null
                || form.getType() == null
                || form.getDescription() == null
                || form.getDescription().isBlank()
                || form.getDescription().length() > 500) {
            throw new IllegalArgumentException(
                    "Verifica el tipo y la descripcion de la solicitud.");
        }
    }

    private Pet findPet(long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la mascota."));
    }

    private SupportRequest findRequest(long petId, long requestId) {
        return supportRequestRepository.findByIdAndPet_Id(requestId, petId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro la solicitud para esta mascota."));
    }
}