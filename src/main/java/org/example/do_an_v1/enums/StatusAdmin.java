package org.example.do_an_v1.enums;

public enum StatusAdmin {

    ACTIVE(1),
    INACTIVE(2);



    private int code;

    StatusAdmin(int code){
        this.code = code;
    }
}
