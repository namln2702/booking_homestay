package org.example.do_an_v1.controller;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.AdvancedHomestaySearchDTO;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HomestayService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/homestays")
public class HomestaySearchController {

    private final HomestayService homestayService;

    @PostMapping("/search/advanced")
    public ApiResponse<?> searchAdvanced(@RequestBody AdvancedHomestaySearchDTO request) {
        return homestayService.searchHomestayAdvanced(request);
    }
}

