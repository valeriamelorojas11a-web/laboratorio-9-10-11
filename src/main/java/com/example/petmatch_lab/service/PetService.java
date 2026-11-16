package com.example.petmatch_lab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.exception.BusinessRuleException;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final SupportRequestRepository supportRequestRepository;

    public PetService(
            PetRepository petRepository,
            SupportRequestRepository supportRequestRepository) {
        this.petRepository = petRepository;
        this.supportRequestRepository = supportRequestRepository;
    }

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public Optional<Pet> findById(long id) {
        return petRepository.findById(id);
    }

    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    @Transactional
    public void deleteById(long id) {
        if (supportRequestRepository.existsByPet_Id(id)) {
            throw new BusinessRuleException(
                "No se puede eliminar esta mascota porque tiene solicitudes registradas."
            );
        }

        petRepository.deleteById(id);
    }
}