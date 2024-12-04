package com.opan.brokerageapi.controllers;

import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.requests.UserLoginRequest;
import com.opan.brokerageapi.requests.UserRegisterRequest;
import com.opan.brokerageapi.security.JwtTokenProvider;
import com.opan.brokerageapi.services.CustomerService;
import com.opan.brokerageapi.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private CustomerService userService;
    private PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CustomerService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserLoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken =  "Bearer "+jwtTokenProvider.generateJwtToken(authentication);
        return ResponseEntity.ok(new ApiResponse<>(true, "You logged in successfully", jwtToken));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody UserRegisterRequest registerRequest) {
        if (userService.getOneUserByEmail(registerRequest.getEmail()) != null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "User already exists", null));
        }

        Customer user = new Customer();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setBalanceTRY(0);

        if (registerRequest.getType() == UserRegisterRequest.UserType.ADMIN) {
            user.getRoles().add("ROLE_ADMIN");
        } else {
            user.getRoles().add("ROLE_CUSTOMER");
        }

        userService.saveOneUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "You registered successfully", null));
    }

}