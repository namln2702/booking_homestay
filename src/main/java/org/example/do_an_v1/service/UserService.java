package org.example.do_an_v1.service;

import org.example.do_an_v1.dto.UserReq;
import org.example.do_an_v1.dto.UserRes;

public interface UserService {

    public UserRes insertUser(UserReq userReq);
}
