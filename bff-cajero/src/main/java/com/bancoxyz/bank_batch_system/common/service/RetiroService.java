package com.bancoxyz.bank_batch_system.common.service;

import com.bancoxyz.bank_batch_system.common.client.dto.RetiroResultadoClienteDTO;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import org.springframework.stereotype.Service;

/**
 * Punto único y reutilizable para pedir un retiro, sin importar el canal.
 * La validación de fondos y la consistencia transaccional ya NO ocurren
 * aquí — ocurren en ms-cuentas, que es quien tiene la fuente de verdad de
 * los datos. Esta clase solo evita que cada canal tenga que saber cómo
 * invocar al microservicio directamente.
 */
@Service
public class RetiroService {

    private final CuentaDataPort dataPort;

    public RetiroService(CuentaDataPort dataPort) {
        this.dataPort = dataPort;
    }

    public RetiroResultadoClienteDTO ejecutar(Long cuentaId, double monto) {
        return dataPort.retirar(cuentaId, monto);
    }
}