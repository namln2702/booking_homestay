package org.example.do_an_v1.enums;

public enum TypePerson {

    ADULTS(1),
    CHILDREN(2),
    BABY(3);



    private int code;

    TypePerson(int code){
        this.code = code;
    }
}
