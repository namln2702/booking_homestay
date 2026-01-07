package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }
    @Override
    public ApiResponse<Boolean> sendSimpleEmail(String email, String code) {

        ApiResponse<Boolean> apiResponse = new ApiResponse<>(200, "Send success", true );
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Confirm email");
            message.setText(code);
            javaMailSender.send(message);
            return apiResponse;
        }catch (Exception e){

            apiResponse.setStatus(404);
            apiResponse.setMessage(e.getMessage());
            apiResponse.setData(false);
            return apiResponse;
        }

    }

    @Override
    public ApiResponse<Boolean> sendComplaintStatusEmail(String customerEmail, String billCode, String oldStatus, String newStatus, String decisionBy) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(200, "Complaint status email sent successfully", true);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Thông báo cập nhật trạng thái khiếu nại - Mã đơn: " + billCode);
            
            String emailBody = buildComplaintStatusEmailBody(billCode, oldStatus, newStatus, decisionBy);
            message.setText(emailBody);
            
            javaMailSender.send(message);
            return apiResponse;
        } catch (Exception e) {
            apiResponse.setStatus(500);
            apiResponse.setMessage("Failed to send complaint status email: " + e.getMessage());
            apiResponse.setData(false);
            return apiResponse;
        }
    }

    private String buildComplaintStatusEmailBody(String billCode, String oldStatus, String newStatus, String decisionBy) {
        StringBuilder body = new StringBuilder();
        body.append("Kính gửi Quý khách hàng,\n\n");
        body.append("Chúng tôi xin thông báo về việc cập nhật trạng thái khiếu nại của đơn hàng của bạn:\n\n");
        body.append("Mã đơn hàng: ").append(billCode).append("\n");
        body.append("Trạng thái cũ: ").append(oldStatus).append("\n");
        body.append("Trạng thái mới: ").append(newStatus).append("\n");
        body.append("Được xử lý bởi: ").append(decisionBy).append("\n\n");
        
        // Thêm thông tin chi tiết dựa trên trạng thái mới
        if (newStatus.contains("REFUNDED") || newStatus.contains("REFUNDED_PENDING")) {
            body.append("Khiếu nại của bạn đã được chấp nhận. Hệ thống đang xử lý hoàn tiền cho bạn.\n");
            body.append("Bạn sẽ nhận được thông báo khi giao dịch hoàn tiền được hoàn tất.\n\n");
        } else if (newStatus.contains("ADMIN_COMPLAINT_PROCESSING")) {
            body.append("Khiếu nại của bạn đã được chuyển cho quản trị viên xem xét.\n");
            body.append("Chúng tôi sẽ thông báo kết quả trong thời gian sớm nhất.\n\n");
        } else if (newStatus.contains("REJECTED")) {
            body.append("Rất tiếc, khiếu nại của bạn đã bị từ chối.\n");
            body.append("Nếu bạn có thắc mắc, vui lòng liên hệ với bộ phận hỗ trợ khách hàng.\n\n");
        }
        
        body.append("Trân trọng,\n");
        body.append("Đội ngũ hỗ trợ khách hàng");
        
        return body.toString();
    }



}
