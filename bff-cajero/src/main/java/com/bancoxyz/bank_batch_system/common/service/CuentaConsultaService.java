package com.bancoxyz.bank_batch_system.common.service;

import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.MovimientoClienteDTO;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Punto único, compartido por los 3 canales, para consultar una cuenta. No
 * conoce HTTP ni JPA — solo delega al puerto de datos. Si en el futuro se
 * necesita agregar caché, reintentos o registro de auditoría para las
 * consultas de cuenta, este es el lugar donde se agrega una sola vez para
 * los 3 canales.
 */
@Service
public class CuentaConsultaService {

    private final CuentaDataPort dataPort;

    public CuentaConsultaService(CuentaDataPort dataPort) {
        this.dataPort = dataPort;
    }

    public EstadoCuentaClienteDTO obtenerEstado(Long cuentaId) {
        return dataPort.buscarEstado(cuentaId);
    }

    public List<MovimientoClienteDTO> obtenerMovimientos(Long cuentaId) {
        return dataPort.buscarMovimientos(cuentaId);
    }
}
