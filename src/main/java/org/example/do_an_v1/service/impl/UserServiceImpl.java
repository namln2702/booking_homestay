package org.example.do_an_v1.service.impl;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.RequestContext;
import org.example.do_an_v1.dto.*;
//import org.example.do_an_v1.dto.SendGoogle;
import org.example.do_an_v1.entity.*;
import org.example.do_an_v1.enums.RoleUser;
import org.example.do_an_v1.enums.Status;
import org.example.do_an_v1.mapper.AdminMapper;
import org.example.do_an_v1.mapper.CustomerMapper;
import org.example.do_an_v1.mapper.HostMapper;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.repository.*;
import org.example.do_an_v1.service.EmailService;
import org.example.do_an_v1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvalidateTokenRepository invalidateTokenRepository;

    @Autowired
    private ConfirmEmailRepository confirmEmailRepository;

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private GoogleRepository googleRepository;

    @Autowired
    private GoogleInfoRepository googleInfoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SecurityService securityService;


    @Value("${outbound.identity.client-id}")
    private String CLIENT_ID ;

    @Value("${outbound.identity.client-secret}")
    private String CLIENT_SECRET;

    @Value("${outbound.identity.client-redirect-uri}")
    private String REDIRECT_URI ;

    private final String GRANT_TYPE = "authorization_code";


    // Check login/register user
    @Transactional
    @Override
    public ApiResponse<CodeForEmail> loginRegWithEmail(String email) throws RuntimeException{

        User user = userRepository.findByEmail(email);


        // Check xem da ton tai trong DB chua
        if(Objects.isNull(user)) {

            user = userRepository.save(User.builder()
                            .email(email)
                    .build());
            customerRepository.save(Customer.builder()
                            .status(Status.INACTIVE)
                            .user(user)
                            .role(RoleUser.CUSTOMER)
                    .build());

        }


        // Check kiem tra co phai Admin khong
        if(Objects.nonNull(user.getAdmin())){
            return new ApiResponse<>(400, "Email invalid", null);
        }

        String code = secureRandomNumbers();


        // Tao code xac thuc
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

    @Transactional
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


        if(Objects.isNull(user)) {

            user = userRepository.save(User.builder()
                    .email(infoUserFromGoogleDTO.getEmail())
                    .build());
            customerRepository.save(Customer.builder()
                    .user(user)
                    .role(RoleUser.CUSTOMER)
                    .build());

        }
        // Check user da ton tai chua
        else if (Objects.nonNull(user.getAdmin())){
            return new ApiResponse<>(400, "Email invalid", null);
        }


        return checkUser(user);
    }


    @Override
    public ApiResponse<?> logout() throws RuntimeException{

        try {

            HttpSession sessionCurrent = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
            sessionCurrent.invalidate();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String tokenId = jwt.getId();
            Date date = Date.from(jwt.getExpiresAt());

            invalidateTokenRepository.save(InvalidateToken.builder()
                    .tokenId(tokenId)
                    .expired(date)
                    .build());
            Object idClaim = jwt.getClaims().get("id");
            if (idClaim instanceof Number number) {
                userRepository.findById(number.longValue()).ifPresent(user -> {
                    if (!Boolean.FALSE.equals(user.getIsOnline())) {
                        user.setIsOnline(false);
                        userRepository.save(user);
                    }
                });
            }
        }
        catch (Exception e){
            throw new  RuntimeException("Token already exits");
        }


        return new ApiResponse<>(200, "Logout success", null);
    }

//    Kiem tra xem email da ton tai hay chua. Neu chua ton tai tao ra 1 customer moi
//    neu co roi tra ve (customer or host or admin)
    public ApiResponse<?> checkUser(User user){

        if (!Boolean.TRUE.equals(user.getIsOnline())) {
            user.setIsOnline(true);
            userRepository.save(user);
        }

        // uu tien check admin truoc
        Admin admin = adminRepository.findByUser(user);
        if (Objects.nonNull(admin)) {
            if (admin.getStatus() != Status.ACTIVE) {
                return new ApiResponse<>(423, "Admin account is not active", null);
            }
            AdminDTO adminDTO = AdminMapper.adminMapAdminDTO(admin);
            String token = securityService.createTokenSystem(user, RoleUser.ADMIN.toString());
            return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder()
                    .token(token)
                    .user(adminDTO)
                    .build());
        }

        Host host = hostRepository.findByUser(user);
        if (Objects.nonNull(host)) {
            // da la host thi phai la customer, vi vay add them row vao bang customer
            ensureCustomerExists(user);

            HostDTO hostDTO = HostMapper.hostMapHostDTO(host);
            String token = securityService.createTokenSystem(user, RoleUser.HOST.toString());

            return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder()
                    .user(hostDTO)
                    .token(token)
                    .build());
        }

        Customer customer = ensureCustomerExists(user);
        CustomerDTO customerDTO = CustomerMapper.toDTO(customer);
        String token = securityService.createTokenSystem(user, RoleUser.CUSTOMER.toString());
        return new ApiResponse<>(200, "Register or Login success", AccessTokenSystemDTO.builder()
                .token(token)
                .user(customerDTO)
                .build());
    }

    private Customer ensureCustomerExists(User user) {
        Customer existing = customerRepository.findByUser(user);
        if (existing != null) {
            return existing;
        }
        Customer created = Customer.builder()
                .user(user)
                .role(RoleUser.CUSTOMER)
                .build();
        return customerRepository.save(created);
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
