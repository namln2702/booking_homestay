package org.example.do_an_v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.do_an_v1.entity.Image;

import java.util.List;


@Getter
@Builder
public class ReviewDTO {

    private Long id;                   // ID của review (kế thừa từ BaseEntity)
    private Integer rating;            // Số sao đánh giá
    private String comment;            // Nội dung bình luận
    private Long homestayId;           // ID homestay được đánh giá
    private Long customerId;           // ID khách hàng
    private List<ImageDTO> imageUrls;    // Danh sách link ảnh review
}
