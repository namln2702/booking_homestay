package org.example.do_an_v1.controller;


import org.example.do_an_v1.configuration.SessionConfig;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/homestay")
public class HomestayController {


    @Autowired
    private SessionConfig sessionConfig;

    @GetMapping
    ApiResponse<?> findHomestay(@RequestBody FindHomeStayDTO findHomeStayDTO){
        return new ApiResponse<>();
    }

    @PutMapping
    ApiResponse<?> updateHomestay(@RequestBody HomestayDTO homestayDTO){

        System.out.println(sessionConfig.httpSession().getAttribute("id"));
        return new ApiResponse<>(200, "Okee", null);
    }

}
