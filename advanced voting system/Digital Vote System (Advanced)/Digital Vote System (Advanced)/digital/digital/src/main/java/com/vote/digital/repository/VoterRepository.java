package com.vote.digital.repository;

import com.vote.digital.model.Voter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VoterRepository extends MongoRepository<Voter, String> {
    Optional<Voter> findByVoterId(String voterId);
}
