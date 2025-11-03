package org.example.do_an_v1.enums;

public enum StatusTransaction {

    PENDING(1),
    SUCCESS(2),
    FAILED(3);



    private int code;

    StatusTransaction(int code){
        this.code = code;
    }
}
