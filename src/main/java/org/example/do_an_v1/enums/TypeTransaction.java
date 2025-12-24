package org.example.do_an_v1.enums;

public enum TypeTransaction {


    BOOKING_PAYMENT(1),
    CUSTOMER_PAYMENT_ADMIN(6),
    CUSTOMER_PAYMENT_ADMIN_FIRST(4),
    CUSTOMER_PAYMENT_ADMIN_SECOND(7),
    ADMIN_PAYMENT_HOST(5),
    REFUND(2),
    PAYLOAD_HOST(3);


    private int code;

    TypeTransaction(int code){
        this.code = code;
    }
}
