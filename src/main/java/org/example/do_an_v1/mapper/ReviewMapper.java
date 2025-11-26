package org.example.do_an_v1.mapper;

import org.example.do_an_v1.dto.ImageDTO;
import org.example.do_an_v1.dto.ReviewDTO;
import org.example.do_an_v1.entity.Image;
import org.example.do_an_v1.entity.Review;

import java.util.stream.Collectors;

public class ReviewMapper {

    /**
     * Convert Review entity -> ReviewDTO
     */
    public static ReviewDTO toDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .homestayId(
                        review.getHomestay() != null ? review.getHomestay().getId() : null
                )
                .customerId(
                        review.getCustomer() != null ? review.getCustomer().getId() : null
                )
                .imageUrls(
                        review.getListImage() != null
                                ? review.getListImage().stream()
                                .map(ImageMapper::toDTO)
                                .collect(Collectors.toList())
                                : null
                )
                .build();
    }



    /**
     * Nếu bạn có nhu cầu convert DTO -> Entity (optional)
     * (Thường dùng khi tạo review mới)
     */
    public static Review toEntity(ReviewDTO dto) {
        if (dto == null) return null;

        return Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
    }

}
