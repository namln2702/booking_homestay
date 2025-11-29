package org.example.do_an_v1.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.enums.StatusHomestay;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.HomestayService;
import org.example.do_an_v1.service.support.RequestIdentityResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/homestay")
public class HomestayController {

    private final HomestayService homestayService;
    private final RequestIdentityResolver identityResolver;

    @GetMapping("/find")
    public ApiResponse<?> findHomestay(@RequestBody FindHomeStayDTO findHomeStayDTO){
        return homestayService.findHomestay(findHomeStayDTO);
    }

    @GetMapping("/all")
    public ApiResponse<?> homestays(
            @RequestParam(name = "status" ) StatusHomestay statusHomestay,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
        return homestayService.getHomestays(statusHomestay , page, size);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_HOST')")
    @GetMapping("/user/used")
    public ApiResponse<?> findUserHistoryHomestays(){
        Long effectiveUserId = identityResolver.requireUserId(null);
        return homestayService.findUserHistoryHomestays(effectiveUserId);
    }

    @GetMapping("/detail")
    public ApiResponse<?> homestay(@RequestParam(name = "id") Long id){
        return homestayService.detailHomestay(id);
    }



//    @PutMapping
//    ApiResponse<?> updateHomestay(@RequestBody HomestayDTO homestayDTO){
//        System.out.println(sessionConfig.httpSession().getAttribute("id"));
//        return new ApiResponse<>(200, "Okee", null);
//    }

}
