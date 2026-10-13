package com.weddingos.os.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.weddingos.os.api.dto.FamilyUpsertRequest;
import com.weddingos.os.api.dto.GuestListUpsertRequest;
import com.weddingos.os.api.dto.GuestUpsertRequest;
import com.weddingos.os.domain.AttendanceStatus;
import com.weddingos.os.domain.Event;
import com.weddingos.os.domain.Wedding;
import com.weddingos.os.repository.EventGuestRepository;
import com.weddingos.os.repository.EventRepository;
import com.weddingos.os.repository.FamilyRepository;
import com.weddingos.os.repository.GuestRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuestListServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private EventGuestRepository eventGuestRepository;

    @InjectMocks
    private GuestListService guestListService;

    @Test
    void rollsBackByThrowingWhenSavingFails() {
        UUID weddingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Wedding wedding = new Wedding();
        wedding.setId(weddingId);
        Event event = new Event();
        event.setId(eventId);
        event.setWedding(wedding);

        when(eventRepository.findByIdAndWeddingId(eventId, weddingId)).thenReturn(Optional.of(event));
        when(familyRepository.save(any())).thenThrow(new RuntimeException("db failure"));

        GuestListUpsertRequest request = new GuestListUpsertRequest(List.of(
                new FamilyUpsertRequest(
                        null,
                        "Reddy Family",
                        "Warangal",
                        null,
                        null,
                        List.of(new GuestUpsertRequest(null, "Ramesh Reddy", null, AttendanceStatus.CONFIRMED, null, null, true, true))
                )
        ));

        assertThatThrownBy(() -> guestListService.saveGuestList(weddingId, eventId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db failure");
    }
}

