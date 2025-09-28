package org.example.do_an_v1.service.impl;

import org.example.do_an_v1.dto.UserReq;
import org.example.do_an_v1.dto.UserRes;
import org.example.do_an_v1.repository.UserRepository;
import org.example.do_an_v1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserRes insertUser(UserReq userReq) {
        return null;
    }


//    String createToken(){};
}
