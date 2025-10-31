package com.mgaye.yonei.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

    @GetMapping
    public Map<String, Object> getDummyData() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("name", "John Doe");
        data.put("email", "john.doe@example.com");
        data.put("role", "Admin");
        data.put("active", true);

        Map<String, Object> address = new HashMap<>();
        address.put("city", "Paris");
        address.put("country", "France");
        address.put("postalCode", "75001");

        data.put("address", address);

        return data; // Spring automatically converts this to JSON
    }
}
