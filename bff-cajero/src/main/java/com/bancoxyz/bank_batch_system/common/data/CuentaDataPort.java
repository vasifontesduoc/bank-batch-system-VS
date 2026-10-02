package com.bancoxyz.bank_batch_system.common.data;

import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.InteresClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.MovimientoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.RetiroResultadoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.TransaccionClienteDTO;

import java.util.List;

/**
 * Puerto de acceso a datos: los servicios de cada BFF dependen únicamente de
 * esta interfaz, nunca de cómo se obtienen los datos en realidad. La
 * implementación actual ({@code HttpCuentaDataAdapter}) llama al
 * microservicio ms-cuentas por HTTP; el BFF ya no tiene acceso directo a la
 * base de datos. Si en el futuro cambia el origen (otro protocolo, otro
 * microservicio, un caché), solo se reemplaza la implementación de este
 * puerto — los servicios y controladores de cada canal no se enteran.
 */
public interface CuentaDataPort {

    EstadoCuentaClienteDTO buscarEstado(Long cuentaId);

    List<MovimientoClienteDTO> buscarMovimientos(Long cuentaId);

    List<TransaccionClienteDTO> listarTransacciones();

    List<TransaccionClienteDTO> listarTransaccionesRecientes(int limite);

    List<InteresClienteDTO> buscarIntereses(Long cuentaId);

    RetiroResultadoClienteDTO retirar(Long cuentaId, Double monto);
}