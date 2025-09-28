package org.example.do_an_v1.enums;

public enum StatusCustomer {

    ACTIVE(1),
    INACTIVE(2);



    private int code;

    StatusCustomer(int code){
        this.code = code;
    }
}
