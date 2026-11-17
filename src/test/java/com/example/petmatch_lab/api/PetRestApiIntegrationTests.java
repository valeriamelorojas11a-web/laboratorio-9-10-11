package com.example.petmatch_lab.api;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petmatch_lab.model.AppUser;
import com.example.petmatch_lab.model.Pet;
import com.example.petmatch_lab.model.SupportRequest;
import com.example.petmatch_lab.repository.AppUserRepository;
import com.example.petmatch_lab.repository.PetRepository;
import com.example.petmatch_lab.repository.SupportRequestRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PetRestApiIntegrationTests {

    private static final String USER_EMAIL = "api.user@example.com";
    private static final String ADMIN_EMAIL = "api.admin@example.com";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SupportRequestRepository requestRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new AppUser("API User", USER_EMAIL, passwordEncoder.encode(PASSWORD)));
        userRepository.save(new AppUser("API Admin", ADMIN_EMAIL, passwordEncoder.encode(PASSWORD)));

        jdbcTemplate.update(
            "UPDATE app_users SET role = 'ADMIN' WHERE email = ?",
            ADMIN_EMAIL
        );
    }

    @Test
    void shouldRejectAnonymousRequestWith401() throws Exception {
        mockMvc.perform(get("/api/v1/pets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUserToListPets() throws Exception {
        Pet pet = new Pet();
        pet.setName("Luna");
        pet.setSpecies("Dog");
        pet.setAge(3);
        petRepository.save(pet);

        mockMvc.perform(get("/api/v1/pets")
                .with(httpBasic(USER_EMAIL, PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Luna"));
    }

    @Test
    void shouldForbidUserFromCreatingPetWith403() throws Exception {
        String body = """
            {"name":"Kira","species":"Dog","age":4,"description":"Friendly"}
            """;

        mockMvc.perform(post("/api/v1/pets")
                .with(httpBasic(USER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());

        assertEquals(0, petRepository.count());
    }

    @Test
    void shouldAllowAdminToCreateUpdateAndDeletePet() throws Exception {
        String createBody = """
            {"name":"Kira","species":"Dog","age":4,"description":"Enjoys walks."}
            """;

        mockMvc.perform(post("/api/v1/pets")
                .with(httpBasic(ADMIN_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Kira"));

        Pet saved = petRepository.findAll().get(0);

        String updateBody = """
            {"name":"Kira","species":"Dog","age":5,"description":"Updated"}
            """;

        mockMvc.perform(put("/api/v1/pets/" + saved.getId())
                .with(httpBasic(ADMIN_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.age").value(5));

        mockMvc.perform(delete("/api/v1/pets/" + saved.getId())
                .with(httpBasic(ADMIN_EMAIL, PASSWORD)))
            .andExpect(status().isNoContent());

        assertEquals(0, petRepository.count());
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        String invalidBody = """
            {"name":"","species":"Dog","age":-2}
            """;

        mockMvc.perform(post("/api/v1/pets")
                .with(httpBasic(ADMIN_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("validation_failed"))
            .andExpect(jsonPath("$.fieldErrors.age").exists());
    }

    @Test
    void shouldReturn404WhenPetDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/pets/999999")
                .with(httpBasic(USER_EMAIL, PASSWORD))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("not_found"));
    }

    @Test
    void shouldManageSupportRequestsAndEnforceBusinessConflict() throws Exception {
        Pet pet = new Pet();
        pet.setName("Milo");
        pet.setSpecies("Cat");
        pet.setAge(2);
        pet = petRepository.save(pet);

        // USER crea solicitud
        String requestBody = """
            {"type":"WALK","description":"Morning walk"}
            """;

        mockMvc.perform(post("/api/v1/pets/" + pet.getId() + "/requests")
                .with(httpBasic(USER_EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("OPEN"));

        SupportRequest createdRequest = requestRepository.findAll().get(0);

        // ADMIN cancela la solicitud
        mockMvc.perform(post("/api/v1/pets/" + pet.getId() + "/requests/" + createdRequest.getId() + "/cancel")
                .with(httpBasic(ADMIN_EMAIL, PASSWORD)))
            .andExpect(status().isNoContent());

        // ADMIN intenta completar una solicitud ya cancelada -> 409 Conflict
        mockMvc.perform(post("/api/v1/pets/" + pet.getId() + "/requests/" + createdRequest.getId() + "/complete")
                .with(httpBasic(ADMIN_EMAIL, PASSWORD)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("business_conflict"));
    }

    @Test
    void shouldKeepCsrfProtectionOnWebPost() throws Exception {
        mockMvc.perform(post("/pets")
                .with(httpBasic(ADMIN_EMAIL, PASSWORD))
                .param("name", "Max")
                .param("species", "Dog")
                .param("age", "2"))
            .andExpect(status().isForbidden());
    }
}