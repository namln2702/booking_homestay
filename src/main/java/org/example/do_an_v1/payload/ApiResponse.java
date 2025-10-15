package org.example.do_an_v1.payload;


import lombok.*;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
