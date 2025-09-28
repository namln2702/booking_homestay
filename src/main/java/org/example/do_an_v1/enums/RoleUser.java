package org.example.do_an_v1.enums;

public enum RoleUser {

    ADMIN(1),
    HOST(2),
    CUSTOMER(3);



    private int code;

    RoleUser(int code){
        this.code = code;
    }
}
