package org.example.do_an_v1.controller;

import lombok.RequiredArgsConstructor;
import org.example.do_an_v1.dto.response.TokenValidationResponse;
import org.example.do_an_v1.payload.ApiResponse;
import org.example.do_an_v1.service.impl.SecurityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tokens")
public class TokenController {

    private final SecurityService securityService;

    /**
     * Kiểm tra token có hết hạn hay không
     * 
     * @param token JWT token (có thể truyền qua header Authorization hoặc query parameter)
     * @return TokenValidationResponse chứa thông tin về trạng thái token
     */
    @GetMapping("/validate")
    public ApiResponse<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        String token = null;
        
        // Ưu tiên lấy từ Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (tokenParam != null) {
            token = tokenParam;
        }
        
        if (token == null || token.trim().isEmpty()) {
            return new ApiResponse<>(
                    400,
                    "Token is required. Please provide token via Authorization header (Bearer <token>) or query parameter (?token=<token>)",
                    null
            );
        }
        
        TokenValidationResponse validationResult = securityService.checkTokenExpiration(token);
        
        int statusCode = validationResult.getIsValid() ? 200 : 401;
        String message = validationResult.getIsValid() 
                ? "Token hợp lệ" 
                : validationResult.getReason();
        
        return new ApiResponse<>(statusCode, message, validationResult);
    }

    /**
     * Kiểm tra token từ request body
     * 
     * @param requestBody Object chứa token
     * @return TokenValidationResponse
     */
    @PostMapping("/validate")
    public ApiResponse<TokenValidationResponse> validateTokenFromBody(
            @RequestBody(required = false) TokenValidationRequest requestBody
    ) {
        String token = null;
        
        if (requestBody != null && requestBody.getToken() != null) {
            token = requestBody.getToken();
        }
        
        if (token == null || token.trim().isEmpty()) {
            return new ApiResponse<>(
                    400,
                    "Token is required in request body: {\"token\": \"<your-jwt-token>\"}",
                    null
            );
        }
        
        TokenValidationResponse validationResult = securityService.checkTokenExpiration(token);
        
        int statusCode = validationResult.getIsValid() ? 200 : 401;
        String message = validationResult.getIsValid() 
                ? "Token hợp lệ" 
                : validationResult.getReason();
        
        return new ApiResponse<>(statusCode, message, validationResult);
    }

    /**
     * Inner class cho request body
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationRequest {
        private String token;
    }
}

