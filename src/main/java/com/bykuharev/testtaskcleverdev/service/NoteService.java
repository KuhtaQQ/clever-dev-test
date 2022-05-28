package com.bykuharev.testtaskcleverdev.service;

import com.bykuharev.testtaskcleverdev.dao.NoteRepository;
import com.bykuharev.testtaskcleverdev.entity.Notes;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class NoteService {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void save(Notes note) {
        try {
            noteRepository.save(note);
            log.info("Note with note_guid {} successful saved", note.getOldNoteGuid());
        } catch (HibernateException e) {
            log.error("Fail to save note with note_guid {}", note.getOldNoteGuid());
        }
    }
}
