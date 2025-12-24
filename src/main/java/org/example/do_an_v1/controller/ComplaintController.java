package org.example.do_an_v1.controller;


import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.ComplaintService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/complaint")
//public class ComplaintController {
//
//
//    private final ComplaintService complaintService;
//
//    @PostMapping("/complaint")
//    ApiResponse<?> userComplaint(@RequestBody ComplaintDTO complaintDTO){
//        return complaintService.userComplaint(complaintDTO);
//    }
//}
