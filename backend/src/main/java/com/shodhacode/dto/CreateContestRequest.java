package com.shodhacode.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateContestRequest {
    @NotBlank
    public String title;
}
