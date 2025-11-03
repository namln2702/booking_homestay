package org.example.do_an_v1.controller;


import org.example.do_an_v1.payload.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
public class PaymentController {

    @PostMapping("/customer_to_admin")
    ApiResponse<?> customerPaymentAdmin(@RequestParam(name = "idTransaction") Long idTracsaction){
        return new ApiResponse<>(200, "Okee", null);
    }
}
