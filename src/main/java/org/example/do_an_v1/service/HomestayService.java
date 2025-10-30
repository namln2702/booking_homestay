package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface HomestayService {

    ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO);

}
