package org.example.do_an_v1.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.dto.*;
//import org.example.do_an_v1.dto.SendGoogle;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.mapper.UserDTOMapUser;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ConfirmEmailRepository confirmEmailRepository;


    @Autowired
    private GoogleRepository googleRepository;

    @Autowired
    private GoogleInfoRepository googleInfoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDTOMapUser userDTOMapUser;

    @Autowired
    private HostRepository hostRepository;

    @Value("${outbound.identity.client-id}")
    private String CLIENT_ID ;

    @Value("${outbound.identity.client-secret}")
    private String CLIENT_SECRET;

    @Value("${outbound.identity.client-redirect-uri}")
    private String REDIRECT_URI ;

    private final String GRANT_TYPE = "authorization_code";


    @Override
    public ApiResponse<CodeForEmail> loginRegWithEmail(String email) throws RuntimeException{

        User user = userRepository.findByEmail(email);

        // Check xem da ton tai trong DB chua
        if(Objects.isNull(user)) {
            user = userRepository.save(User.builder()
                            .email(email)
                    .build());
        }
        String code = secureRandomNumbers();

        ConfirmEmail confirmEmail = confirmEmailRepository.save(ConfirmEmail.builder()
                        .code(code)
                        .email(user.getEmail())
                        .expired_at(LocalDateTime.now().plus(5, ChronoUnit.MINUTES))
                .build());


        // Gui code toi mail nguoi dung
        emailService.sendSimpleEmail(user.getEmail(), code);

        ApiResponse<CodeForEmail> apiResponse = new ApiResponse<>(200, "Send code for verify email",
                CodeForEmail.builder()
                        .email(user.getEmail())
                        .userId(user.getId())
                        .confirmEmailId(confirmEmail.getId())
                        .code(code)
                .build());

        return apiResponse;
    }

    @Override
    public ApiResponse<?> confirmEmail(CodeForEmail codeForEmail) {

        ConfirmEmail confirmEmail = confirmEmailRepository.findByEmailAndCode(codeForEmail.getEmail(), codeForEmail.getCode());

        // Check code nguoi dung gui ve
        if(Objects.isNull(confirmEmail)){
            return new ApiResponse<>(422, "Incorrect code", false);
        }
        else{
            if(LocalDateTime.now().isAfter(confirmEmail.getExpired_at())){
                return new ApiResponse<>(422, "Expired code", false);
            }
        }
        User user = userRepository.findById(codeForEmail.getUserId()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        return checkUser(user);

    }

    @Override
    public ApiResponse<?> loginRegEmailWithGoogle(String authCode) throws RuntimeException {
        // Lay accessToken
        AccessTokenGoogleDTO responseGetAccToken = getAccessTokenDTO(authCode);
        InfoUserFromGoogleDTO infoUserFromGoogleDTO = googleInfoRepository.getInfoUserFromGoogleDTO("Bearer " + responseGetAccToken.getAccessToken());

        if(Objects.isNull(infoUserFromGoogleDTO)){
            return new ApiResponse<>(400, "Khong ton tai thong tin Email", null);
        }
        log.info(infoUserFromGoogleDTO.toString());

        User user = userRepository.findByEmail(infoUserFromGoogleDTO.getEmail());


        return checkUser(user);
    }


    public ApiResponse<?> checkUser(User user){
        if(Objects.isNull(user)){
            return new ApiResponse<>(200, "Register customer success", customerRepository.save(Customer.builder()
                    .user(user)
                    .build()));
        }

        else {
            Customer customer = customerRepository.findByUser(user);
            if(Objects.nonNull(customer)){
                return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder()
                        .build());
            }

            Host host = hostRepository.findByUser(user);
            if(Objects.nonNull(host)){
                return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder().build());
            }
        }

        return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder()
                .user(customerRepository.save(Customer.builder()
                        .user(user)
                        .build()))
                .build());

    }

    public AccessTokenGoogleDTO getAccessTokenDTO(String authCode) throws RuntimeException{
        AccessTokenGoogleDTO responseGetAccToken = googleRepository.getAccessToken(SendGoogle.builder()
                .code(authCode)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());
        log.info("TOKEN RESPONSE {}", responseGetAccToken);

        if(Objects.isNull(responseGetAccToken) || responseGetAccToken.getAccessToken().isEmpty() || responseGetAccToken.getAccessToken().isBlank()){
            throw new RuntimeException("Email khong hop le");
        }

        return responseGetAccToken;
    }
    // Create code
    public String secureRandomNumbers() {


        CustomerDTO customerDTO = CustomerDTO.builder()

                .build();

        SecureRandom secureRandom = new SecureRandom();

        String result = IntStream.range(0,6)
                .map(i -> secureRandom.nextInt(10))
                .mapToObj(String:: valueOf)
                .collect(Collectors.joining());

        return result;
    }

}
