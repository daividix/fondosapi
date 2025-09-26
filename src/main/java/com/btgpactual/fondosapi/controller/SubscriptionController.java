package com.btgpactual.fondosapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.btgpactual.fondosapi.dto.ApiResponse;
import com.btgpactual.fondosapi.dto.CancellationRequest;
import com.btgpactual.fondosapi.dto.SubscriptionRequest;
import com.btgpactual.fondosapi.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<String>> subscribe(@Valid @RequestBody SubscriptionRequest req) {
        String txId = subscriptionService.subscribe(req);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "Suscripción creada", txId));
    }

    @PostMapping("/cancellations")
    public ResponseEntity<ApiResponse<String>> cancel(@Valid @RequestBody CancellationRequest req) {
        String txId = subscriptionService.cancel(req.getAccountId(), req.getTransactionId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Cancelación procesada", txId));
    }
}
