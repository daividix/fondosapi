package com.btgpactual.fondosapi.controller;

import com.btgpactual.fondosapi.dto.ApiResponse;
import com.btgpactual.fondosapi.dto.CancellationRequest;
import com.btgpactual.fondosapi.dto.SubscriptionRequest;
import com.btgpactual.fondosapi.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/_health")
public class HealthController {

    public HealthController() {
        
    }

    @GetMapping
    public ResponseEntity<String> isOk() {
        return ResponseEntity.ok("ok");
    }
}
