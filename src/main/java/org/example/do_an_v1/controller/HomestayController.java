package org.example.do_an_v1.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
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
            @RequestParam(name = "status" , defaultValue = "ACTIVE") StatusHomestay statusHomestay,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
        return homestayService.getHomestays(statusHomestay , page, size);
    }

    @GetMapping("/detail")
    public ApiResponse<?> homestay(@RequestParam(name = "id") Long id){
        return homestayService.detailHomestay(id);
    }


    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping()
    public ApiResponse<HomestayDTO> createHomestayForCurrentHost(
            @RequestBody @Valid HomestayCreateRequest request
    ) {
        Long effectiveUserId = identityResolver.requireUserId(null);
        System.out.println("HostController.createHomestayForCurrentHost: " + effectiveUserId );
        return homestayService.createHomestay(effectiveUserId, request);
    }


}
