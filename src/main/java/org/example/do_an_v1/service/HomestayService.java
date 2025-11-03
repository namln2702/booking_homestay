package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.HomestayDTO;
import org.example.do_an_v1.dto.request.HomestayCreateRequest;
import org.example.do_an_v1.payload.ApiResponse;

public interface HomestayService {

    ApiResponse<HomestayDTO> createHomestay(Long hostUserId, HomestayCreateRequest request);

    ApiResponse<HomestayDTO> approveHomestay(Long adminUserId, Long homestayId);
}
