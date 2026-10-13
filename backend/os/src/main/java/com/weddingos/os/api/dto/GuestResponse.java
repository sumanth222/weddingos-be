package com.weddingos.os.api.dto;

import com.weddingos.os.domain.AttendanceStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GuestResponse(
        UUID guestId,
        String fullName,
        String phone,
        AttendanceStatus attendanceStatus,
        OffsetDateTime arrivalAt,
        OffsetDateTime departureAt,
        OffsetDateTime effectiveArrivalAt,
        OffsetDateTime effectiveDepartureAt,
        boolean needsAccommodation,
        boolean needsTransport
) {
}

