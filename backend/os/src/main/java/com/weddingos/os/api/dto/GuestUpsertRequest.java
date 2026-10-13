package com.weddingos.os.api.dto;

import com.weddingos.os.domain.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GuestUpsertRequest(
        UUID guestId,
        @NotBlank(message = "fullName is required")
        String fullName,
        @Pattern(regexp = "^$|^[0-9+()\\- ]{7,20}$", message = "phone must be a valid phone number")
        String phone,
        @NotNull(message = "attendanceStatus is required")
        AttendanceStatus attendanceStatus,
        OffsetDateTime arrivalAt,
        OffsetDateTime departureAt,
        boolean needsAccommodation,
        boolean needsTransport
) {
}

