package org.example.do_an_v1.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.entity.User;
import org.example.do_an_v1.repository.InvalidateTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;



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


    public String createTokenSystem(User u, String role){


        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(u.getEmail())
                .issuer("lainguyennam270203@gmail.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("id", u.getId())
                .claim("scope","ROLE_" + role) // ROLE_ADMIN
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }catch (JOSEException e){
            log.error("Cannot create token ", e);
            throw new RuntimeException(e.getMessage());
        }

    }


    // Can nhac lam refresh token
    public void verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        String tokenId = signedJWT.getJWTClaimsSet().getJWTID();

        if(invalidateTokenRepository.existsById(tokenId)){
            throw new RuntimeException("Token invalidate");
        }

        // Check expiryTime
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Check private key
        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new RuntimeException("Token error or expired");
        }

        if (invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new RuntimeException("Token has been deleted ");
        }

    }
}
