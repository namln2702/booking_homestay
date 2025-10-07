package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.dto.CodeForEmail;
import org.example.do_an_v1.dto.UserDTO;
import org.example.do_an_v1.dto.UserRes;
import org.example.do_an_v1.entity.ConfirmEmail;
import org.example.do_an_v1.entity.Users;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.mapper.UserDTOMapUser;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.ConfirmEmailRepository;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmEmailRepository confirmEmailRepository;


    @Autowired
    private EmailService emailService;

    private UserDTOMapUser userDTOMapUser;

    @Override
    public ApiResponse<CodeForEmail> createVerifyCode(UserDTO userDTO) throws RuntimeException{

        Users user = userRepository.findByEmail(userDTO.getEmail());

        if (Objects.nonNull(user)){
            throw new RuntimeException("Email exits");
        }


        // Luu nguoi dung vao User voi verifyEmal = false
        Users nowUser = userRepository.save(userDTOMapUser.userDTOMapUser(userDTO));
        nowUser.setEmailVerify(false);

        String code = secureRandomNumbers();
        ConfirmEmail confirmEmail = confirmEmailRepository.save(ConfirmEmail.builder()
                        .code(code)
                        .email(nowUser.getEmail())
                        .expired_at(LocalDateTime.now())
                .build());

        ApiResponse<CodeForEmail> apiResponse = new ApiResponse<>(200, "Send code for verify email",
                CodeForEmail.builder()
                        .email(nowUser.getEmail())
                        .userId(nowUser.getId())
                        .confirmEmailId(confirmEmail.getId())
                .build());

        return apiResponse;
    }

    @Override
    public ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) {

        ConfirmEmail confirmEmail = confirmEmailRepository.findByEmailAndCode(codeForEmail.getEmail(), codeForEmail.getCode());

        if(Objects.isNull(confirmEmail)){
            return new ApiResponse<>(422, "Incorrect code", false);
        }
        else{
            if(LocalDateTime.now().isAfter(confirmEmail.getExpired_at())){
                return new ApiResponse<>(422, "Expired code", false);
            }
        }

        Users user = userRepository.findById(codeForEmail.getUserId()).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        user.setEmailVerify(true);

        return new ApiResponse<>(200, "Verify success", user);


    }


    // Create code
    public String secureRandomNumbers() {
        SecureRandom secureRandom = new SecureRandom();

        String result = IntStream.range(0,6)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String:: valueOf)
                .collect(Collectors.joining());

        return result;
    }

}
