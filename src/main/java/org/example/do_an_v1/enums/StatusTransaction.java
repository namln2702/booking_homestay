package org.example.do_an_v1.enums;

public enum StatusTransaction {

    PENDING(1),
    PROCESSING(2),
    SUCCESS(3),
    FAILED(4);



    private int code;

    StatusTransaction(int code){
        this.code = code;
    }
}
