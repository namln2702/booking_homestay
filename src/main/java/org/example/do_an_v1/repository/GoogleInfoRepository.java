package org.example.do_an_v1.repository;

import org.example.do_an_v1.dto.AccessTokenGoogleDTO;
import org.example.do_an_v1.dto.InfoUserFromGoogleDTO;
import org.example.do_an_v1.dto.SendGoogle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "google-userinfo", url = "https://www.googleapis.com")
public interface GoogleInfoRepository {
    @GetMapping(value = "/oauth2/v1/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    InfoUserFromGoogleDTO getInfoUserFromGoogleDTO(
            @RequestHeader("Authorization") String bearerToken);
}
