package org.example.do_an_v1.controller;


import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/homestay")
public class HomestayController {

    @PostMapping
    ApiResponse<?> findHomestay(@RequestBody FindHomeStayDTO findHomeStayDTO){
        return new ApiResponse<>();
    }

}
