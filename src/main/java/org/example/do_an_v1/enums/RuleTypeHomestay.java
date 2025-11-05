package org.example.do_an_v1.enums;

public enum RuleTypeHomestay {

    ALLOW(1),
    NOT_ALLOW(2);



    private int code;

    RuleTypeHomestay(int code){
        this.code = code;
    }
}
