package com.minimart.api.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BranchRequestDto(
        Long id,
        @NotNull(message = "Province id is required")
        Long province_id,

        @NotBlank(message = "Branch code is required")
        @Size(max = 30, message = "Branch code must not exceed 30 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Branch code may only contain letters, numbers, underscores, and hyphens"
        )
        String branch_code,

        @NotBlank(message = "Branch name is required")
        @Size(max = 150, message = "Branch name must not exceed 150 characters")
        String branch_name,

        @NotNull(message = "Active status is required")
        Boolean is_active
) {
}
