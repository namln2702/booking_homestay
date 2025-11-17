package org.example.do_an_v1.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.ComplaintDTO;
import org.example.do_an_v1.entity.Bill;
import org.example.do_an_v1.entity.Complaint;
import org.example.do_an_v1.entity.Image;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.BillRepository;
import org.example.do_an_v1.repository.ComplaintRepository;
import org.example.do_an_v1.repository.ImageRepository;
import org.example.do_an_v1.service.ComplaintService;
import org.example.do_an_v1.utils.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final BillRepository billRepository;
    private final ImageRepository imageRepository;
    private final ComplaintRepository complaintRepository;

    @Value("${claim-expiration-date}")
    private Integer claimExpirationDate;


    @Override
    public ApiResponse<?> userComplaint(ComplaintDTO complaintDTO){

        // Find bill
        Bill bill = billRepository.findById(complaintDTO.getBillId()).orElseThrow(
                () -> new RuntimeException("Bill not exits")
        );


        // Check time for Complaint
        Integer checkExpired = Date.compareDate(bill.getCreatedAt().plusDays(claimExpirationDate), complaintDTO.getCreatedAt());
        if(checkExpired == 1) {
            return new ApiResponse<>(422, "Expired for complaint", null);
        }


        // Save images
        Set<Image> images = new HashSet<>();
        complaintDTO.getImageUrls().forEach(image -> {
            images.add(
                    imageRepository.save(Image.builder()
                            .image_url(image)
                            .build())
            );
        });

        // Save complaint
        Complaint complaint = Complaint.builder()
                .bill(bill)
                .description(complaintDTO.getDescription())
                .listImage(images)
                .build();

        Complaint complaintResult = complaintRepository.save(complaint);

        return new ApiResponse<>(200, "Create complaint success", complaintResult);
    }


}
