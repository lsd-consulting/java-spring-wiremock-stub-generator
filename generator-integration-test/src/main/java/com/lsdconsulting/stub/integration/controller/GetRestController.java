package com.lsdconsulting.stub.integration.controller;

import com.lsdconsulting.stub.integration.model.Greeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/getController")
public class GetRestController {

    @GetMapping("/resourceWithNoParams")
    public Greeting resourceWithNoParams() {
        return Greeting.builder().name("name").build();
    }
}
