package com.weddingos.os.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FamilyResponse(
        UUID familyId,
        String familyName,
        String originCity,
        OffsetDateTime defaultArrivalAt,
        OffsetDateTime defaultDepartureAt,
        List<GuestResponse> guests
) {
}

