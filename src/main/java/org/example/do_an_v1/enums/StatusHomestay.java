package org.example.do_an_v1.enums;

public enum StatusHomestay {

    BAN(1),
    ACTIVE(2),
    INACTIVE(3),

    PENDING(4);



    private int code;

    StatusHomestay(int code){
        this.code = code;
    }
}
