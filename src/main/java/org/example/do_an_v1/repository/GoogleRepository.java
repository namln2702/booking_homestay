package org.example.do_an_v1.repository;
import org.example.do_an_v1.dto.AccessTokenGoogleDTO;
import org.example.do_an_v1.dto.InfoUserFromGoogleDTO;
import org.example.do_an_v1.dto.SendGoogle;
import org.springframework.http.MediaType;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "google-oauth", url = "https://oauth2.googleapis.com")
public interface GoogleRepository {
    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    AccessTokenGoogleDTO getAccessToken(@ModelAttribute SendGoogle sendGoogle);
}
