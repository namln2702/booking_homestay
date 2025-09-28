package org.example.do_an_v1.enums;

public enum LevelAdmin {
    ADMIN(1),
    SUPER_ADMIN(2);



    private int code;

    LevelAdmin(int code){
        this.code = code;
    }
}
