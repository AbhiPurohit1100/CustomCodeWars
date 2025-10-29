package com.shodhacode.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateProblemRequest {
    @NotBlank
    public String title;
    @NotBlank
    public String statement;
}
