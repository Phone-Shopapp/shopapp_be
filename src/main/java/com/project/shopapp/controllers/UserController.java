package com.project.shopapp.controllers;

import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.LoginResponse;
import com.project.shopapp.responses.RegisterResponse;
import com.project.shopapp.services.IUserService;
import com.project.shopapp.utils.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.shopapp.dtos.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(RegisterResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.REGISTER_FAILED, errorMessages.toString()))
                    .build());
        }
        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            return ResponseEntity.badRequest().body(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_NOT_MATCH))
                            .build());
        }
        try {
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.REGISTER_SUCCESSFULLY))
                            .user(user)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    RegisterResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO) {
        // Kiểm tra thông tin đăng nhập và sinh token
        try {

            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword()
            );
            // Trả về token trong response
            return ResponseEntity.ok(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(token)
                            .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                            .build());
        }
    }
}
