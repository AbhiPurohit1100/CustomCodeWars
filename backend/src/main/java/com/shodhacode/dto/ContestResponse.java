package com.shodhacode.dto;

import java.util.List;

public class ContestResponse {
    public Long id;
    public String title;
    public List<ProblemDto> problems;

    public ContestResponse(Long id, String title, List<ProblemDto> problems) {
        this.id = id; this.title = title; this.problems = problems;
    }
}
