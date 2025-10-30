package org.example.do_an_v1.controller;


import org.example.do_an_v1.service.support.GcsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/image")
public class UploadController {

    @Autowired
    private GcsService gcsService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("please specify file to upload", HttpStatus.BAD_REQUEST);
        }

        try {
            // Gọi GcsService để upload và lấy URL
            String publicUrl = gcsService.uploadFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Uploaded the file successfully!");
            response.put("pictureUrl", publicUrl);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("server error when uploading: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

