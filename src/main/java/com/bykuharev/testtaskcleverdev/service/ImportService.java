package com.bykuharev.testtaskcleverdev.service;

import com.bykuharev.testtaskcleverdev.dto.NoteDto;
import com.bykuharev.testtaskcleverdev.dto.PatientDto;
import com.bykuharev.testtaskcleverdev.dto.ResponseNoteDTO;
import com.bykuharev.testtaskcleverdev.entity.Notes;
import com.bykuharev.testtaskcleverdev.entity.Patient;
import com.bykuharev.testtaskcleverdev.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
@Transactional
public class ImportService {

    private final PatientService patientService;
    private final NoteService noteService;
    private final UserService userService;
    private final WebClientService webClientService;

    @Autowired
    public ImportService(PatientService patientService, NoteService noteService,
                         UserService userService, WebClientService webClientService) {
        this.patientService = patientService;
        this.noteService = noteService;
        this.userService = userService;
        this.webClientService = webClientService;
    }

    @Scheduled (cron = "${interval-in-cron}")
    public void importAllData(){
        Map<String, Patient> oldGuidFromPatient = patientService.getGuid();
        if (oldGuidFromPatient.isEmpty()) {
            log.info("There are no patient with active status in the new system");
            return;
        }

        List<ResponseNoteDTO> oldSystemNotes = getOldNotes(oldGuidFromPatient);
        if (oldSystemNotes.isEmpty()) {
            log.info("There are no notes in the old system");
            return;
        }

        List<Notes> guidNewNotes = oldGuidFromPatient.values().stream()
                .map(Patient::getNotes)
                .flatMap(Collection::stream)
                .collect(toList());

        saveOrUpdateNote(oldGuidFromPatient, oldSystemNotes, guidNewNotes);
        log.info("Data import from the old system to the new one was completed successfully");
    }

    private void saveOrUpdateNote(Map<String, Patient> oldGuidFromPatient, List<ResponseNoteDTO> oldSystemNotes,
                                  List<Notes> guidNewNotes) {
        oldSystemNotes.forEach(oldNote -> {
            Patient patient = oldGuidFromPatient.get(oldNote.getClientGuid());
            Set<String> newNoteGuids = guidNewNotes.stream()
                    .map(Notes::getOldNoteGuid)
                    .collect(toSet());
            if (newNoteGuids.contains(oldNote.getClientGuid())) {
                Notes newNote = guidNewNotes.stream()
                        .filter(note -> note.getOldNoteGuid().equals(oldNote.getClientGuid()))
                        .findFirst().orElse(null);
                checkAndUpdateNote(oldNote, newNote, patient);
            } else {
                log.info("New note with note_guid:{} for patient_guid:{} has been saved", oldNote.getClientGuid(), patient.getOldClientGuid());
                save(oldNote, patient);
            }
        });
    }

    private void checkAndUpdateNote(ResponseNoteDTO oldNote, Notes newNote, Patient patient) {
        if (oldNote!=null && replacement(oldNote, newNote) && checkModifiedDate(oldNote, newNote)) {
            log.info("Note with note_guid: {} for patient_guid: {} has been updated ", oldNote.getClientGuid(), patient.getOldClientGuid());
            save(oldNote, patient);
        }
    }

    private boolean checkModifiedDate(ResponseNoteDTO oldNote, Notes newNote) {
        return oldNote.getModifiedDateTime().isAfter(newNote.getLastModifiedDateTime());

    }

    private void save(ResponseNoteDTO oldNote, Patient patient) {
        User user = userService.findByLoginOrCreate(oldNote.getLoggedUser());
        Notes note = Notes.builder()
                .createdDateTime(oldNote.getCreatedDateTime())
                .createdUser(user)
                .lastModifiedDateTime(oldNote.getModifiedDateTime())
                .lastModifiedUser(user)
                .note(oldNote.getComments())
                .patient(patient)
                .oldNoteGuid(oldNote.getGuid())
                .build();
        noteService.save(note);
    }

    private boolean replacement(ResponseNoteDTO oldNote, Notes newNote) {
        return !oldNote.getComments().equals(newNote.getNote())
                || !oldNote.getModifiedDateTime().equals(newNote.getLastModifiedDateTime())
                || !oldNote.getCreatedDateTime().equals(newNote.getCreatedDateTime())
                || !oldNote.getLoggedUser().equals(newNote.getCreatedUser().getLogin());
    }

    private List<ResponseNoteDTO> getOldNotes(Map<String, Patient> guidActivePatients) {
        List<NoteDto> requestNote = createRequestNote(guidActivePatients);
        return webClientService.getNotes(requestNote);
    }

    private List<NoteDto> createRequestNote(Map<String, Patient> guid) {
        List<PatientDto> clients = webClientService.getClients();
        return clients.stream()
                .filter(client -> guid.containsKey(client.getGuid()))
                .map(this::buildNoteForNewSystem)
                .collect(toList());
    }

    private NoteDto buildNoteForNewSystem(PatientDto client) {
        return NoteDto.builder()
                .agency(client.getAgency())
                .dateFrom(client.getCreatedDateTime().toLocalDate())
                .dateTo(LocalDate.now())
                .clientGuid(client.getGuid())
                .build();
    }
}
