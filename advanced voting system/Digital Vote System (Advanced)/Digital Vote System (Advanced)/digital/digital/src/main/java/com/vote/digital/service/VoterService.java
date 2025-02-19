package com.vote.digital.service;

import com.vote.digital.model.Vote;
import com.vote.digital.repository.VoteRepository;
import com.vote.digital.model.Voter;
import com.vote.digital.repository.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class VoterService {

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private VoteRepository voteRepository;

    public void saveVoter(String name, String voterId, String phone, String photoData) {
        Voter voter = new Voter();
        voter.setName(name);
        voter.setVoterId(voterId);
        voter.setPhone(phone);
        voter.setPhoto(photoData);
        voterRepository.save(voter);
        System.out.println("Database connected: Voter data saved.");
    }

    public boolean verifyVoterId(String voterId, String name) {
        System.out.println("Connecting to the database for voter ID verification...");
        Optional<Voter> voterOptional = voterRepository.findByVoterId(voterId);
        if (voterOptional.isPresent() && voterOptional.get().getName().equals(name)) {
            System.out.println("Voter ID verified: " + voterId);
            return true;
        } else {
            System.out.println("Voter ID verification failed: " + voterId);
            return false;
        }
    }

    public Map<String, Object> verifyVoterPhoto(String voterId, String newPhotoBase64) throws IOException {
        System.out.println("Connecting to the database for voter photo verification...");
        Optional<Voter> voterOptional = voterRepository.findByVoterId(voterId);
        if (voterOptional.isPresent()) {
            Voter voter = voterOptional.get();

            String dbPhotoBase64 = voter.getPhoto();
            String dbPhotoPath = "F:\\advanced voting system\\Photo Matching\\Photo Matching\\Images\\db_photo.png";
            String newPhotoPath = "F:\\advanced voting system\\Photo Matching\\Photo Matching\\Images\\new_photo.png";

            // Save base64 images to files
            System.out.println("Saving database photo to file...");
            saveBase64ToFile(dbPhotoBase64, dbPhotoPath);
            System.out.println("Saving new photo to file...");
            saveBase64ToFile(newPhotoBase64, newPhotoPath);

            System.out.println("Calling Python script for face verification...");
            String result = callPythonScript(dbPhotoPath, newPhotoPath);

            if (result != null) {
                System.out.println("Python script output: " + result);
                String[] lines = result.trim().split("\n");
                for (String line : lines) {
                    if (line.startsWith("Prediction confidence:")) {
                        String[] parts = line.split(":");
                        double confidence = Double.parseDouble(parts[1].trim());
                        return Map.of("verified", confidence < 50.088, "confidence", confidence);
                    }
                }
            } else {
                System.out.println("Failed to get output from Python script.");
                return Map.of("verified", false, "confidence", -1.0);
            }
        }
        System.out.println("Voter photo verification failed: Voter ID not found.");
        return Map.of("verified", false, "confidence", -1.0);
    }

    public boolean castVote(String voterId, String party) {
        System.out.println("Casting vote for voter ID: " + voterId);
        try {
            Vote vote = new Vote();
            vote.setVoterId(voterId);
            vote.setParty(party);
            voteRepository.save(vote);
            System.out.println("Vote cast successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Error casting vote: " + e.getMessage());
            return false;
        }
    }

    private void saveBase64ToFile(String base64String, String filePath) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String.split(",")[1]);
        Files.write(Paths.get(filePath), decodedBytes);
        System.out.println("Image saved to: " + filePath);
    }

    private String callPythonScript(String dbPhotoPath, String newPhotoPath) {
        String pythonInterpreter = "C:\\Program Files\\Python313\\python.exe";
        String scriptPath = "F:\\advanced voting system\\Photo Matching\\Photo Matching\\verify_photo.py";
        String[] cmd = new String[] { pythonInterpreter, scriptPath, dbPhotoPath, newPhotoPath };
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);

        // Print environment variables and paths
        Map<String, String> env = processBuilder.environment();
        System.out.println("Environment variables:");
        for (String key : env.keySet()) {
            System.out.println(key + " = " + env.get(key));
        }

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method for user signup
    public void signupVoter(String name, String voterId, String phone, String photoData) {
        System.out.println("Starting signup process...");
        saveVoter(name, voterId, phone, photoData);
        System.out.println("Signup completed: Voter data stored in database.");
    }
}
