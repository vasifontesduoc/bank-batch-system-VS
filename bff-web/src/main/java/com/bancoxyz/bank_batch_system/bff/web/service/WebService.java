package com.bancoxyz.bank_batch_system.bff.web.service;

import com.bancoxyz.bank_batch_system.bff.web.dto.EstadoCuentaWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.InteresWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.TransaccionWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.mapper.WebMapper;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import com.bancoxyz.bank_batch_system.common.service.CuentaConsultaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BFF Web: respuestas completas y detalladas. No conoce el microservicio de
 * datos ni construye DTOs a mano — delega la búsqueda a
 * {@link CuentaConsultaService} / {@link CuentaDataPort} y la transformación
 * a {@link WebMapper} (mapeo centralizado).
 */
@Service
public class WebService {

    private final CuentaConsultaService consultaService;
    private final CuentaDataPort dataPort;
    private final WebMapper mapper;

    public WebService(CuentaConsultaService consultaService, CuentaDataPort dataPort, WebMapper mapper) {
        this.consultaService = consultaService;
        this.dataPort = dataPort;
        this.mapper = mapper;
    }

    public EstadoCuentaWebDTO obtenerEstadoCuenta(Long cuentaId) {
        var estado = consultaService.obtenerEstado(cuentaId);
        var movimientos = consultaService.obtenerMovimientos(cuentaId);
        return mapper.toEstadoCuentaDTO(estado, movimientos);
    }

    public List<TransaccionWebDTO> listarTransacciones() {
        return dataPort.listarTransacciones().stream().map(mapper::toTransaccionDTO).toList();
    }

    public List<InteresWebDTO> historialIntereses(Long cuentaId) {
        return dataPort.buscarIntereses(cuentaId).stream().map(mapper::toInteresDTO).toList();
    }
}