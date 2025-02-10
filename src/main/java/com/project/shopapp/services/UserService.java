package com.project.shopapp.services;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.utils.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;

    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException, PermissionDenyException {
        String phoneNumber = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            String errorMes = localizationUtils.getLocalizeMessage(MessageKeys.PHONE_EXISTS);
            throw new DataIntegrityViolationException(errorMes);
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> {
                        String errorMes = localizationUtils.getLocalizeMessage(MessageKeys.ROLE_NOT_FOUND);
                        return new DataNotFoundException(errorMes);
                });
        if (role.getName().equals(Role.ADMIN)) {
            String errorMes = localizationUtils.getLocalizeMessage(MessageKeys.REGISTER_NOT_ADMIN);
            throw new PermissionDenyException(errorMes);
        }
        //convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();


        newUser.setRole(role);
        // Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);

            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws DataNotFoundException {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        if (userOptional.isEmpty()) {
            String errorMes = localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_PASSWORD_INCORRECT);
            throw new DataNotFoundException(errorMes);
        }
        User user = userOptional.get();
        if(user.getFacebookAccountId() == 0 && user.getGoogleAccountId() == 0) {
            if(!passwordEncoder.matches(password, user.getPassword())) {
                String errorMes = localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_PASSWORD_INCORRECT);
                throw new DataNotFoundException(errorMes);
            }
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getPhoneNumber(), password,
                user.getAuthorities()
        );

        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtils.generateToken(user);
    }
}

