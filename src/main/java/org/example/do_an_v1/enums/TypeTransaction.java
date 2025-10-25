package org.example.do_an_v1.enums;

public enum TypeTransaction {


    BOOKING_PAYMENT(1),
    REFUND(2),
    PAYLOAD_HOST(3);


    private int code;

    TypeTransaction(int code){
        this.code = code;
    }
}
