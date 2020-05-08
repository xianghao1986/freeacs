package com.github.freeacs.controllers;

import com.github.freeacs.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
public class CommandControllerTest {
    @Autowired
    protected MockMvc mvc;
    @Test
    public void submitCommand() throws Exception {
        MockHttpServletRequestBuilder postRequestBuilder = post("/setting/setParamList");
        String req = getFileAsString("/command/setParam.json");
        req = req.replace("123456", "TestCommand-" + System.currentTimeMillis());
        mvc.perform(postRequestBuilder
                .content(req))
                .andExpect(status().isOk())
                .andExpect(content().string("00"));

    }
}