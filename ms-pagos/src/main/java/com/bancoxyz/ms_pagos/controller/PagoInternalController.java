package com.bancoxyz.ms_pagos.controller;

import com.bancoxyz.ms_pagos.dto.PagoRequest;
import com.bancoxyz.ms_pagos.dto.PagoResponse;
import com.bancoxyz.ms_pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/pagos")
public class PagoInternalController {

    private final PagoService service;

    public PagoInternalController(PagoService service) {
        this.service = service;
    }

    @PostMapping
    public PagoResponse procesar(@Valid @RequestBody PagoRequest request) {
        return service.procesarPago(request);
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public List<PagoResponse> listar() {
        return service.listar();
    }
}
