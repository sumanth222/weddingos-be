package com.weddingos.os.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GuestListUpsertRequest(
        @NotEmpty(message = "families must not be empty")
        List<@Valid FamilyUpsertRequest> families
) {
}

