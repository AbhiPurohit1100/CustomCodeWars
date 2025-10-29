package com.shodhacode.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTestCaseRequest {
    @NotBlank
    public String inputText;
    @NotBlank
    public String expectedOutput;
    public Integer orderIndex;
}
