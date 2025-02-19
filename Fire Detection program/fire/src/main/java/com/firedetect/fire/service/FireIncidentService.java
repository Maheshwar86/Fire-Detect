package com.firedetect.fire.service;

import com.firedetect.fire.model.FireDetectionIncident;
import com.firedetect.fire.repo.FireIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FireIncidentService {

    @Autowired
    private FireIncidentRepository fireIncidentRepository;

    public FireDetectionIncident saveFireIncident(FireDetectionIncident fireDetectionIncident) {
        fireDetectionIncident.setTimestamp(LocalDateTime.now());
        return fireIncidentRepository.save(fireDetectionIncident);
    }

    public List<FireDetectionIncident> getAllFireIncidents() {
        return fireIncidentRepository.findAll();
    }

    public Optional<FireDetectionIncident> getFireIncidentById(String id) {
        return fireIncidentRepository.findById(id);
    }

    public void deleteFireIncident(String id) {
        fireIncidentRepository.deleteById(id);
    }
}
