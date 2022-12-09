package com.minwonhaeso.esc.stadium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(StadiumUserController.class)
class StadiumUserControllerTest {
    @MockBean
    private StadiumService stadiumService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


}