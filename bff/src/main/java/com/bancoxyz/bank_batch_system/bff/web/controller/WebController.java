package com.bancoxyz.bank_batch_system.bff.web.controller;

import com.bancoxyz.bank_batch_system.bff.web.dto.EstadoCuentaWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.InteresWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.TransaccionWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.service.WebService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BFF Web (requiere header X-API-KEY con la key de canal WEB).
 * Expone vistas completas y detalladas, pensadas para pantallas de escritorio
 * con espacio para mostrar tablas e historiales completos.
 */
@RestController
@RequestMapping("/api/web")
public class WebController {

    private final WebService service;

    public WebController(WebService service) {
        this.service = service;
    }

    @GetMapping("/cuentas/{cuentaId}/estado-anual")
    public EstadoCuentaWebDTO estadoAnual(@PathVariable Long cuentaId) {
        return service.obtenerEstadoCuenta(cuentaId);
    }

    @GetMapping("/transacciones")
    public List<TransaccionWebDTO> transacciones() {
        return service.listarTransacciones();
    }

    @GetMapping("/cuentas/{cuentaId}/intereses")
    public List<InteresWebDTO> intereses(@PathVariable Long cuentaId) {
        return service.historialIntereses(cuentaId);
    }
}
