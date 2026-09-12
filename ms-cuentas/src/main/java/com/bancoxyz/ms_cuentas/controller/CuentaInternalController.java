package com.bancoxyz.ms_cuentas.controller;

import com.bancoxyz.ms_cuentas.dto.*;
import com.bancoxyz.ms_cuentas.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API interna del microservicio de datos. No la consumen clientes finales
 * directamente — la consume el BFF (Web/Móvil/Cajero) para obtener los datos
 * que luego transforma según cada canal.
 */
@RestController
@RequestMapping("/internal")
public class CuentaInternalController {

    private final CuentaService service;

    public CuentaInternalController(CuentaService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/estado")
    public EstadoCuentaResponse estado(@PathVariable Long cuentaId) {
        return service.obtenerEstado(cuentaId);
    }

    @GetMapping("/cuentas/{cuentaId}/movimientos")
    public List<MovimientoResponse> movimientos(@PathVariable Long cuentaId) {
        return service.obtenerMovimientos(cuentaId);
    }

    @GetMapping("/transacciones")
    public List<TransaccionResponse> transacciones() {
        return service.listarTransacciones();
    }

    @GetMapping("/transacciones/recientes")
    public List<TransaccionResponse> transaccionesRecientes(@RequestParam(defaultValue = "5") int limite) {
        return service.listarTransaccionesRecientes(limite);
    }

    @GetMapping("/intereses/{cuentaId}")
    public List<InteresResponse> intereses(@PathVariable Long cuentaId) {
        return service.obtenerIntereses(cuentaId);
    }

    @PostMapping("/cuentas/{cuentaId}/retiro")
    public RetiroResponse retirar(@PathVariable Long cuentaId, @Valid @RequestBody RetiroRequest request) {
        return service.retirar(cuentaId, request.monto());
    }
}
