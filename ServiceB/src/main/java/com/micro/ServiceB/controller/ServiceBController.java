package com.micro.ServiceB.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/b")
public class ServiceBController {

    @GetMapping
    public ResponseEntity<String> serviceB() {
        String msg= "Service B is called from Service A";
        return new ResponseEntity<String>(msg, HttpStatus.OK);
    }


}
