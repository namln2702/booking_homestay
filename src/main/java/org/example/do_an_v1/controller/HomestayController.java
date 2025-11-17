package org.example.do_an_v1.controller;


import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.ComplaintService;
import org.example.do_an_v1.service.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/homestay")
public class HomestayController {

    private  SessionConfig sessionConfig;
    private final HomestayService homestayService;

    @GetMapping("/find")
    ApiResponse<?> findHomestay(@RequestBody FindHomeStayDTO findHomeStayDTO){
        return homestayService.findHomestay(findHomeStayDTO);
    }

    @GetMapping("/all")
    ApiResponse<?> getAllHomestay(){
        return null;
    }
    @GetMapping("/user/find")
    ApiResponse<?> findHomestayWithUser(){
        return null;
    }

    @GetMapping("/user/review")
    ApiResponse<?> reviewHomestay(@RequestBody ReviewDTO reviewDTO){
        return null;
    }

    @PutMapping
    ApiResponse<?> updateHomestay(@RequestBody HomestayDTO homestayDTO){
        System.out.println(sessionConfig.httpSession().getAttribute("id"));
        return new ApiResponse<>(200, "Okee", null);
    }

}
