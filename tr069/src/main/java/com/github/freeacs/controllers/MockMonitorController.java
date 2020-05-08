package com.github.freeacs.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MockMonitorController {


    @PostMapping(value = "/report")
    public String report(@RequestBody String reportBody) {
        log.info("mock：" +  reportBody);
        return "success";
    }
}
