package com.bancoxyz.bank_batch_system.bff.mobile.controller;

import com.bancoxyz.bank_batch_system.bff.mobile.dto.SaldoMobileDTO;
import com.bancoxyz.bank_batch_system.bff.mobile.dto.TransaccionMobileDTO;
import com.bancoxyz.bank_batch_system.bff.mobile.service.MobileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BFF Móvil (requiere header X-API-KEY con la key de canal MOBILE).
 * Expone payloads livianos: solo los campos esenciales, sin metadatos
 * adicionales, pensados para minimizar el consumo de datos.
 */
@RestController
@RequestMapping("/api/mobile")
public class MobileController {

    private final MobileService service;

    public MobileController(MobileService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public SaldoMobileDTO saldo(@PathVariable Long cuentaId) {
        return service.obtenerSaldo(cuentaId);
    }

    @GetMapping("/transacciones/recientes")
    public List<TransaccionMobileDTO> recientes(@RequestParam(defaultValue = "5") int limite) {
        return service.transaccionesRecientes(limite);
    }
}
