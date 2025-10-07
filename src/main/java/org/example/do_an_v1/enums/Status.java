package org.example.do_an_v1.enums;

public enum Status {

    ACTIVE(1),
    INACTIVE(2);



    private int code;

    Status(int code){
        this.code = code;
    }
}
