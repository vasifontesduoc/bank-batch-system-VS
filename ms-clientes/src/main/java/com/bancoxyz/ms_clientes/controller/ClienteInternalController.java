package com.bancoxyz.ms_clientes.controller;

import com.bancoxyz.ms_clientes.dto.*;
import com.bancoxyz.ms_clientes.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/clientes")
public class ClienteInternalController {

    private final ClienteService service;

    public ClienteInternalController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return service.crear(request);
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/{id}/resumen-financiero")
    public ResumenFinancieroResponse resumenFinanciero(@PathVariable Long id) {
        return service.resumenFinanciero(id);
    }
}
