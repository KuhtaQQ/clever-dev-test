package com.bykuharev.testtaskcleverdev.service;

import com.bykuharev.testtaskcleverdev.dto.NoteDto;
import com.bykuharev.testtaskcleverdev.dto.PatientDto;
import com.bykuharev.testtaskcleverdev.dto.ResponseNoteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class WebClientService {

    public static final String URL_NOTES = "/notes";
    public static final String URL_CLIENTS = "/clients";

    private final WebClient webClient;

    @Autowired
    public WebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<PatientDto> getClients() {
        return webClient
                .post()
                .uri(URL_CLIENTS)
                .retrieve()
                .bodyToFlux(PatientDto.class)
                .collectList()
                .block();
    }

    public List<ResponseNoteDTO> getNotes(List<NoteDto> requestNotes) {
        return requestNotes.stream()
                .map(this::getNote)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public List<ResponseNoteDTO> getNote(NoteDto requestBodyNote) {
        return webClient
                .post()
                .uri(URL_NOTES)
                .bodyValue(requestBodyNote)
                .retrieve()
                .bodyToFlux(ResponseNoteDTO.class)
                .collectList()
                .block();
    }
}
