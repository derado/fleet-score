package com.fleetscore.club.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSailingClubRequest(
        @NotBlank(message = "Club name is required")
        @Size(max = 200, message = "Club name must not exceed 200 characters")
        String name,
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,
        @Size(max = 100, message = "Place must not exceed 100 characters")
        String place,
        @Size(max = 20, message = "Post code must not exceed 20 characters")
        String postCode,
        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address,
        @Size(max = 100, message = "Email must not exceed 100 characters")
        @Email(message = "Invalid email format")
        String email,
        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,
        Long organisationId
) {}
