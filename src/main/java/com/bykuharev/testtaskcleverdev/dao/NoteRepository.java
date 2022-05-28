package com.bykuharev.testtaskcleverdev.dao;

import com.bykuharev.testtaskcleverdev.entity.Notes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Notes, Long> {
}
