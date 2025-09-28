package org.example.do_an_v1.enums;

public enum StatusToken {

    ACTIVE(1),
    INACTIVE(2);



    private int code;

    StatusToken(int code){
        this.code = code;
    }
}
