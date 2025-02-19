package com.firedetect.fire.controller;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.firedetect.fire.model.FireDetectionIncident;
import com.firedetect.fire.service.FireIncidentService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

@Controller
public class WebController {

    private static final Logger logger = Logger.getLogger(WebController.class.getName());

    @Autowired
    private FireIncidentService fireIncidentService;

    static {
        // Load the native library
        Loader.load(org.bytedeco.opencv.global.opencv_core.class);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/data")
    public String data() {
        return "data";
    }

    @PostMapping("/saveData")
    public String saveData(@RequestParam("latitude") String latitude,
                           @RequestParam("longitude") String longitude,
                           @RequestParam("name") String name,
                           @RequestParam("areaName") String areaName,
                           @RequestParam("imageUrl") String imageUrl) {
        FireDetectionIncident incident = new FireDetectionIncident();
        incident.setLatitude(latitude);
        incident.setLongitude(longitude);
        incident.setName(name);
        incident.setAreaName(areaName);
        incident.setImageUrl(imageUrl);
        fireIncidentService.saveFireIncident(incident);
        return "redirect:/";
    }

    @PostMapping("/detectFire")
    @ResponseBody
    public Map<String, Boolean> detectFire(@RequestBody Map<String, String> requestBody) {
        String imageUrl = requestBody.get("imageUrl");
        boolean fireDetected = false;

        try {
            logger.info("Received image data for fire detection.");

            // Decode the base64 image
            byte[] decodedBytes = Base64.getDecoder().decode(imageUrl.split(",")[1]);
            Mat image = opencv_imgcodecs.imdecode(new Mat(new BytePointer(decodedBytes)), opencv_imgcodecs.IMREAD_COLOR);
            logger.info("Image decoded successfully.");

            // Verify that the directory exists and is writable
            String directoryPath = "F:\\Fire Detection program\\FireDetect\\Fire Images"; // Update this path
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the scanned image temporarily (use a supported extension like .jpg or .png)
            String scannedImagePath = directoryPath + "\\ScannedImage.jpg"; // Update this path
            boolean result = opencv_imgcodecs.imwrite(scannedImagePath, image);

            if (!result) {
                throw new RuntimeException("Failed to write the image file. Make sure the file extension is supported.");
            }

            // Call the Python script to detect fire
            ProcessBuilder processBuilder = new ProcessBuilder("python", "F:\\Fire Detection program\\FireDetect\\detect.py", scannedImagePath); // Update this path
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            logger.info("Output from Python script:");
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.info(line);  // Log output from Python script
                if (line.contains("Fire detected!")) {
                    fireDetected = true;
                }
            }
            process.waitFor();
            logger.info("Full Python script output: " + output.toString());

        } catch (Exception e) {
            logger.severe("Error during fire detection: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("fireDetected", fireDetected);
        return response;
    }

    @GetMapping("/api/firedata")
    @ResponseBody
    public ResponseEntity<List<FireDetectionIncident>> getFireData() {
        List<FireDetectionIncident> incidents = fireIncidentService.getAllFireIncidents();
        return ResponseEntity.ok(incidents);
    }

    @DeleteMapping("/api/firedata/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteFireData(@PathVariable("id") String id) {
        Optional<FireDetectionIncident> incidentOptional = fireIncidentService.getFireIncidentById(id);
        if (incidentOptional.isPresent()) {
            fireIncidentService.deleteFireIncident(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
