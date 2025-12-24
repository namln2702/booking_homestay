package org.example.do_an_v1.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;

import java.util.List;

/**
 * Request payload to update the authenticated customer's profile.
 * Booleans track whether a field is explicitly provided so we can support partial updates.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProfileUpdateRequest {

    private String name;
    private boolean nameProvided;
    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    private String phone;
    private boolean phoneProvided;
    @JsonSetter("phone")
    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneProvided = true;
    }

    private String dateOfBirth;
    private boolean dateOfBirthProvided;
    @JsonSetter("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.dateOfBirthProvided = true;
    }

    private Integer age;
    private boolean ageProvided;
    @JsonSetter("age")
    public void setAge(Integer age) {
        this.age = age;
        this.ageProvided = true;
    }

    private String avatarUrl;
    private boolean avatarUrlProvided;
    @JsonSetter("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.avatarUrlProvided = true;
    }

    private String qrCodeUrl;
    private boolean qrCodeUrlProvided;
    @JsonSetter("qrCodeUrl")
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
        this.qrCodeUrlProvided = true;
    }

    private List<Long> listPreference;
    private boolean listPreferenceProvided;
    @JsonSetter("listPreference")
    public void setListPreference(List<Long> listPreference) {
        this.listPreference = listPreference;
        this.listPreferenceProvided = true;
    }
}
