package com.routesync.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class TestController {

    @GetMapping("/passenger/test")
    public ResponseEntity<String> passenger() {

        return ResponseEntity.ok(
                "Success"
        );
    }

    @GetMapping("/driver/test")
    public ResponseEntity<String> driver() {

        return ResponseEntity.ok(
                "Success"
        );
    }

    @GetMapping("/admin/test")
    public ResponseEntity<String> admin() {

        return ResponseEntity.ok(
                "Success"
        );
    }

}
