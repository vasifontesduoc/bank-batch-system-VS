package com.bancoxyz.bank_batch_system.bff.cajero.controller;

import com.bancoxyz.bank_batch_system.bff.cajero.dto.RetiroRequestDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.dto.RetiroResponseDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.dto.SaldoCajeroDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.service.CajeroService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BFF Cajero (requiere header X-API-KEY con la key de canal CAJERO).
 * Expone únicamente las dos operaciones críticas de un cajero automático:
 * consultar saldo y retirar. Sin listados ni historiales — solo lo esencial
 * para una interfaz segura y acotada.
 */
@RestController
@RequestMapping("/api/cajero")
public class CajeroController {

    private final CajeroService service;

    public CajeroController(CajeroService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public SaldoCajeroDTO saldo(@PathVariable Long cuentaId) {
        return service.consultarSaldo(cuentaId);
    }

    @PostMapping("/cuentas/{cuentaId}/retiro")
    public RetiroResponseDTO retirar(@PathVariable Long cuentaId, @Valid @RequestBody RetiroRequestDTO request) {
        return service.retirar(cuentaId, request.monto());
    }
}
