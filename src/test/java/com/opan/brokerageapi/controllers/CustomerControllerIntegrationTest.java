package com.opan.brokerageapi.controllers;

import com.opan.brokerageapi.entities.Customer;
import com.opan.brokerageapi.repositories.CustomerRepository;
import com.opan.brokerageapi.requests.DepositRequestDto;
import com.opan.brokerageapi.requests.UserLoginRequest;
import com.opan.brokerageapi.requests.UserRegisterRequest;
import com.opan.brokerageapi.requests.WithdrawRequestDto;
import com.opan.brokerageapi.utils.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private Long testCustomerId;

    @BeforeEach
    @Transactional
    public void setUp() throws Exception {
        customerRepository.deleteAll();

        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setPassword("password");
        registerRequest.setType(UserRegisterRequest.UserType.CUSTOMER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("testuser@example.com");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseString = loginResult.getResponse().getContentAsString();

        ApiResponse<String> loginResponse = objectMapper.readValue(loginResponseString, new TypeReference<ApiResponse<String>>() {});

        jwtToken = loginResponse.getData();

        Customer testCustomer = customerRepository.findByEmail("testuser@example.com");
        testCustomerId = testCustomer.getId();
    }

    @Test
    public void testDepositMoney() throws Exception {
        DepositRequestDto depositRequest = new DepositRequestDto();
        depositRequest.setAmountTRY(500);

        mockMvc.perform(post("/api/customers/" + testCustomerId + "/deposit")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Deposit successful")));

        Customer updatedCustomer = customerRepository.findById(testCustomerId).orElse(null);
        assert updatedCustomer != null;
        assert updatedCustomer.getBalanceTRY() != null;
        assert updatedCustomer.getBalanceTRY().equals(500);
    }

    @Test
    public void testWithdrawMoney() throws Exception {
        testDepositMoney();

        WithdrawRequestDto withdrawRequest = new WithdrawRequestDto();
        withdrawRequest.setAmountTRY(200);
        withdrawRequest.setIban("TR330006100519786457841326");

        mockMvc.perform(post("/api/customers/" + testCustomerId + "/withdraw")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Withdraw successful")));

        Customer updatedCustomer = customerRepository.findById(testCustomerId).orElse(null);
        assert updatedCustomer != null;
        assert updatedCustomer.getBalanceTRY() != null;
        assert updatedCustomer.getBalanceTRY().equals(500 - 200);
    }
}