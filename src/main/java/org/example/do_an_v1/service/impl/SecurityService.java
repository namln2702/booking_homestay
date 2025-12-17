package org.example.do_an_v1.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.dto.response.TokenValidationResponse;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.repository.InvalidateTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
@Slf4j
public class SecurityService {



    @Value("${jwt.valid-duration}")
    private long VALID_DURATION;

    @Value("${jwt.refresh-token}")
    private long REFRESHABLE_DURATION;

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Autowired
    private InvalidateTokenRepository invalidateTokenRepository;


    public String createTokenSystem(User u, String role) throws JOSEException {
        // Giữ lại method cũ để backward compatibility
        return createTokenSystem(u, List.of(role));
    }

    public String createTokenSystem(User u, List<String> roles) throws JOSEException {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles list cannot be null or empty");
        }

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        // Tạo scope string từ danh sách roles: "ROLE_ADMIN ROLE_HOST ROLE_CUSTOMER"
        String scope = roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .collect(Collectors.joining(" "));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(u.getEmail())
                .issuer("lainguyennam270203@gmail.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("id", u.getId())
                .claim("scope", scope) // "ROLE_ADMIN ROLE_HOST ROLE_CUSTOMER"
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }catch (JOSEException e){
            log.error("Cannot create token ", e);
            throw new JOSEException("Cannot create token: " + e.getMessage());
        }

    }


    // Can nhac lam refresh token
    public void verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        String tokenId = signedJWT.getJWTClaimsSet().getJWTID();

        if(invalidateTokenRepository.existsById(tokenId)){
            throw new JOSEException("Token invalidate");
        }

        // Check expiryTime
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Check private key
        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new JOSEException("Token error or expired");
        }

        if (invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new JOSEException("Token has been deleted");
        }

    }

    /**
     * Kiểm tra token có hợp lệ hay không (tận dụng method verifyToken có sẵn)
     * @param token JWT token string
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean isTokenValid(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra token và trả về lý do nếu token hư
     * @param token JWT token string
     * @return TokenValidationResponse
     */
    public TokenValidationResponse checkTokenExpiration(String token) {
        try {
            verifyToken(token);
            return TokenValidationResponse.builder()
                    .isValid(true)
                    .reason(null)
                    .build();
        } catch (JOSEException e) {
            return TokenValidationResponse.builder()
                    .isValid(false)
                    .reason(e.getMessage())
                    .build();
        } catch (ParseException e) {
            return TokenValidationResponse.builder()
                    .isValid(false)
                    .reason("Token không đúng định dạng")
                    .build();
        } catch (Exception e) {
            return TokenValidationResponse.builder()
                    .isValid(false)
                    .reason("Token không hợp lệ")
                    .build();
        }
    }
}
