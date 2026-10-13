package com.weddingos.os.service;

import com.weddingos.os.api.dto.FamilyResponse;
import com.weddingos.os.api.dto.FamilyUpsertRequest;
import com.weddingos.os.api.dto.GuestListResponse;
import com.weddingos.os.api.dto.GuestListUpsertRequest;
import com.weddingos.os.api.dto.GuestResponse;
import com.weddingos.os.api.dto.GuestUpsertRequest;
import com.weddingos.os.domain.AttendanceStatus;
import com.weddingos.os.domain.Event;
import com.weddingos.os.domain.EventGuest;
import com.weddingos.os.domain.Family;
import com.weddingos.os.domain.Guest;
import com.weddingos.os.repository.EventGuestRepository;
import com.weddingos.os.repository.EventRepository;
import com.weddingos.os.repository.FamilyRepository;
import com.weddingos.os.repository.GuestRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GuestListService {

    private final EventRepository eventRepository;
    private final FamilyRepository familyRepository;
    private final GuestRepository guestRepository;
    private final EventGuestRepository eventGuestRepository;

    public GuestListService(
            EventRepository eventRepository,
            FamilyRepository familyRepository,
            GuestRepository guestRepository,
            EventGuestRepository eventGuestRepository
    ) {
        this.eventRepository = eventRepository;
        this.familyRepository = familyRepository;
        this.guestRepository = guestRepository;
        this.eventGuestRepository = eventGuestRepository;
    }

    @Transactional
    public GuestListResponse saveGuestList(String weddingId, String eventId, GuestListUpsertRequest request) {
        UUID weddingUuid = parseUuid(weddingId, "weddingId");
        UUID eventUuid = parseUuid(eventId, "eventId");

        Event event = eventRepository.findByIdAndWeddingId(eventUuid, weddingUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<FamilyUpsertRequest> familyRequests = request.families();
        if (familyRequests == null || familyRequests.isEmpty()) {
            throw new BusinessValidationException("families must not be empty");
        }

        List<FamilyResponse> responses = new ArrayList<>();
        for (FamilyUpsertRequest familyRequest : familyRequests) {
            responses.add(saveFamily(weddingUuid, event, familyRequest));
        }

        return new GuestListResponse(weddingUuid, event.getId(), responses);
    }

    private FamilyResponse saveFamily(UUID weddingId, Event event, FamilyUpsertRequest request) {
        Family family = new Family();
        family.setId(request.familyId() != null ? request.familyId() : UUID.randomUUID());
        family.setWeddingId(weddingId);
        family.setFamilyName(request.familyName());
        family.setOriginCity(request.originCity());
        family.setDefaultArrivalAt(request.defaultArrivalAt());
        family.setDefaultDepartureAt(request.defaultDepartureAt());
        familyRepository.save(family);

        List<GuestResponse> guestResponses = new ArrayList<>();
        for (GuestUpsertRequest guestRequest : request.guests()) {
            Guest guest = saveGuest(weddingId, family.getId(), guestRequest);
            guestResponses.add(toGuestResponse(guest));
            eventGuestRepository.save(toEventGuest(event.getId(), guest));
        }

        return new FamilyResponse(
                family.getId(),
                family.getFamilyName(),
                family.getOriginCity(),
                family.getDefaultArrivalAt(),
                family.getDefaultDepartureAt(),
                guestResponses
        );
    }

    private Guest saveGuest(UUID weddingId, UUID familyId, GuestUpsertRequest request) {
        Guest guest = new Guest();
        guest.setId(request.guestId() != null ? request.guestId() : UUID.randomUUID());
        guest.setWeddingId(weddingId);
        guest.setFamilyId(familyId);
        guest.setFullName(request.fullName());
        guest.setPhone(StringUtils.hasText(request.phone()) ? request.phone() : null);
        guest.setAttendanceStatus(request.attendanceStatus());
        guest.setArrivalAt(request.arrivalAt());
        guest.setDepartureAt(request.departureAt());
        guest.setNeedsAccommodation(request.needsAccommodation());
        guest.setNeedsTransport(request.needsTransport());
        return guestRepository.save(guest);
    }

    private EventGuest toEventGuest(UUID eventId, Guest guest) {
        EventGuest eventGuest = new EventGuest();
        eventGuest.setId(UUID.randomUUID());
        eventGuest.setEventId(eventId);
        eventGuest.setGuestId(guest.getId());
        eventGuest.setAttendanceStatus(guest.getAttendanceStatus() == null ? AttendanceStatus.PENDING : guest.getAttendanceStatus());
        return eventGuest;
    }

    private GuestResponse toGuestResponse(Guest guest) {
        return new GuestResponse(
                guest.getId(),
                guest.getFullName(),
                guest.getPhone(),
                guest.getAttendanceStatus(),
                guest.getArrivalAt(),
                guest.getDepartureAt(),
                guest.isNeedsAccommodation(),
                guest.isNeedsTransport()
        );
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(fieldName + " must be a valid UUID");
        }
    }
}

