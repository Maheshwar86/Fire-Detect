package com.firedetect.fire.repo;

import com.firedetect.fire.model.FireDetectionIncident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FireIncidentRepository extends MongoRepository<FireDetectionIncident, String> {
}
