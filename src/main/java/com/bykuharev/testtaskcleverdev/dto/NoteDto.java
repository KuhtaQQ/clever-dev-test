package com.bykuharev.testtaskcleverdev.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class NoteDto {

    private String agency;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String clientGuid;
}
