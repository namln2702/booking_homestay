package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.dto.FindHomeStayDTO;
import org.example.do_an_v1.entity.Homestay;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.HomestayRepository;
import org.example.do_an_v1.service.HomestayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HomestayServiceImpl implements HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;
    @Override
    public ApiResponse<?> findHomestay(FindHomeStayDTO findHomeStayDTO) {
        List<Homestay> homestayList = homestayRepository.findHomestay(
                findHomeStayDTO.getAddress(),
                findHomeStayDTO.getNumberAdults(),
                findHomeStayDTO.getNumberChildren(),
                findHomeStayDTO.getNumberBaby(),
                findHomeStayDTO.getBegin(),
                findHomeStayDTO.getEnd()
        );
    }
}
