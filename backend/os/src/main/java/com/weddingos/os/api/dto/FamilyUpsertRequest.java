package com.weddingos.os.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FamilyUpsertRequest(
        UUID familyId,
        @NotBlank(message = "familyName is required")
        String familyName,
        String originCity,
        OffsetDateTime defaultArrivalAt,
        OffsetDateTime defaultDepartureAt,
        @NotNull(message = "guests is required")
        List<@Valid GuestUpsertRequest> guests
) {
}
