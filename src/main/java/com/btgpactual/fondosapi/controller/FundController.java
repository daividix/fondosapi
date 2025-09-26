package com.btgpactual.fondosapi.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.btgpactual.fondosapi.model.Fund;
import com.btgpactual.fondosapi.repository.FundRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/funds")
@CrossOrigin(origins = "*")
public class FundController {

    private final FundRepository fundRepository;

    public FundController(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @GetMapping
    public List<Fund> listFunds() {
        return fundRepository.findAll();
    }
}
