
package com.example.petmatch_lab.model;

import com.example.petmatch_lab.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupportRequestTests {

    private SupportRequest createRequest() {
        Pet pet = new Pet(
                null,
                "Luna",
                "Perro",
                2,
                "Mascota de prueba"
        );

        return new SupportRequest(
                pet,
                SupportType.WALK,
                "Necesita un paseo"
        );
    }

    @Test
    void shouldStartAsOpen() {
        SupportRequest request = createRequest();

        assertEquals(RequestStatus.OPEN, request.getStatus());
        assertTrue(request.isOpen());
    }

    @Test
    void shouldCompleteOpenRequest() {
        SupportRequest request = createRequest();

        request.complete();

        assertEquals(RequestStatus.COMPLETED, request.getStatus());
        assertFalse(request.isOpen());
    }

    @Test
    void shouldCancelOpenRequest() {
        SupportRequest request = createRequest();

        request.cancel();

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
        assertFalse(request.isOpen());
    }

    @Test
    void shouldRejectCompletingCompletedRequest() {
        SupportRequest request = createRequest();
        request.complete();

        assertThrows(BusinessRuleException.class, request::complete);
    }

    @Test
    void shouldRejectCancellingCompletedRequest() {
        SupportRequest request = createRequest();
        request.complete();

        assertThrows(BusinessRuleException.class, request::cancel);
    }

    @Test
    void shouldRejectCompletingCancelledRequest() {
        SupportRequest request = createRequest();
        request.cancel();

        assertThrows(BusinessRuleException.class, request::complete);
    }
}
