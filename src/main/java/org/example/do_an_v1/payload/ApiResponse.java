package org.example.do_an_v1.payload;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class ApiResponse <T>{
    private int status;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}
