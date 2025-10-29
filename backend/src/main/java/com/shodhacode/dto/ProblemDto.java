package com.shodhacode.dto;

public class ProblemDto {
    public Long id;
    public String title;
    public String statement;
    public ProblemDto(Long id, String title, String statement) {
        this.id = id; this.title = title; this.statement = statement;
    }
}
