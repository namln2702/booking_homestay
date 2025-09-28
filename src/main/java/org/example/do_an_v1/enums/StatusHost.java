package org.example.do_an_v1.enums;

public enum StatusHost {

    PENDING(1),
    ACTIVE(2),
    INACTIVE(3);



    private int code;

    StatusHost(int code){
        this.code = code;
    }
}
