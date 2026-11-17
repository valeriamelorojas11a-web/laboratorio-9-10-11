package com.example.petmatch_lab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petmatch_lab.form.PetForm;
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
    public Pet create(PetForm form) {
        Pet pet = new Pet();
        pet.setName(form.getName() == null ? null : form.getName().trim());
        pet.setSpecies(form.getSpecies() == null ? null : form.getSpecies().trim());
        pet.setAge(form.getAge());
        String description = (form.getDescription() == null || form.getDescription().isBlank())
                ? null
                : form.getDescription().trim();
        pet.setDescription(description);
        return petRepository.save(pet);
    }

    @Transactional
    public Optional<Pet> update(long id, PetForm form) {
        return petRepository.findById(id).map(pet -> {
            pet.setName(form.getName() == null ? null : form.getName().trim());
            pet.setSpecies(form.getSpecies() == null ? null : form.getSpecies().trim());
            pet.setAge(form.getAge());
            String description = (form.getDescription() == null || form.getDescription().isBlank())
                    ? null
                    : form.getDescription().trim();
            pet.setDescription(description);
            return petRepository.save(pet);
        });
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

    @Transactional
    public boolean delete(long id) {
        if (!petRepository.existsById(id)) {
            return false;
        }
        deleteById(id);
        return true;
    }
}