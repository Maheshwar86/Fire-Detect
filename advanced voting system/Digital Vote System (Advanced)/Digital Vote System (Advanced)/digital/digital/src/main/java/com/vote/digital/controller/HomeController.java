package com.vote.digital.controller;

import com.vote.digital.service.VoterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private VoterService voterService;

    @GetMapping("/")
    public String index() {
        return "index"; // Make sure the file is located in src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@RequestParam("name") String name,
                               @RequestParam("voterId") String voterId,
                               @RequestParam("phone") String phone,
                               @RequestParam("photoData") String photoData) {

        voterService.saveVoter(name, voterId, phone, photoData);
        return "redirect:/";
    }

    @PostMapping("/verify-id")
    @ResponseBody
    public Map<String, Boolean> verifyVoterId(@RequestBody Map<String, String> request) {
        String voterId = request.get("voterId");
        String name = request.get("name");

        boolean verified = voterService.verifyVoterId(voterId, name);
        return Map.of("success", verified);
    }

    @PostMapping("/verify-photo")
    @ResponseBody
    public Map<String, Object> verifyVoterPhoto(@RequestBody Map<String, String> request) throws IOException {
        String voterId = request.get("voterId");
        String photoData = request.get("photoData");

        Map<String, Object> verificationResult = voterService.verifyVoterPhoto(voterId, photoData);
        return verificationResult;
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("voterId") String voterId,
                              @RequestParam("name") String name,
                              @RequestParam("photoData") String photoData,
                              Model model) throws IOException {
        if (voterService.verifyVoterId(voterId, name)) {
            Map<String, Object> verificationResult = voterService.verifyVoterPhoto(voterId, photoData);
            model.addAttribute("verificationResult", verificationResult.get("verified"));
            model.addAttribute("confidence", verificationResult.get("confidence"));
        } else {
            model.addAttribute("verificationResult", false);
            model.addAttribute("confidence", -1.0);
        }
        return "login";
    }

    @PostMapping("/cast-vote")
    @ResponseBody
    public Map<String, Boolean> castVote(@RequestBody Map<String, String> request) {
        String voterId = request.get("voterId");
        String party = request.get("party");

        boolean success = voterService.castVote(voterId, party);
        return Map.of("success", success);
    }

    @GetMapping("/vote")
    public String vote() {
        return "vote";
    }
}
