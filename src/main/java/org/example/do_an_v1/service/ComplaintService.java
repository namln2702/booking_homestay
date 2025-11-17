package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.payload.ApiResponse;

public interface ComplaintService {

    ApiResponse<?> userComplaint(ComplaintDTO complaintDTO);


}
