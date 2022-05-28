package com.bykuharev.testtaskcleverdev.service;

import com.bykuharev.testtaskcleverdev.dao.PatientRepository;
import com.bykuharev.testtaskcleverdev.entity.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
@Slf4j
public class PatientService {

    public static final String SPLITTER = ",";

    public final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }
    public void fillGuid(Map<String, Patient> guidPatients, Patient patient) {
        String oldGuid = patient.getOldClientGuid();
        String[] split = oldGuid.split(SPLITTER);
        Arrays.stream(split)
                .forEach(guid -> guidPatients.put(guid, patient));
    }
    public Map <String, Patient> getGuid(){
        Map<String, Patient> guid = new HashMap<>();
        List<Patient> patients = findPatients();
        if (patients!= null) {
            patients.stream()
                    .filter(patient -> (patient.getOldClientGuid()!=null))
                    .forEach(patient -> fillGuid(guid, patient));
        }
        return guid;
    }

    public List<Patient> findPatients() {
        return patientRepository.findPatientByStatus();
    }
}
