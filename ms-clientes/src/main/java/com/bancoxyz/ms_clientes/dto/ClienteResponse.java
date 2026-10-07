package com.bancoxyz.ms_clientes.dto;

public record ClienteResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String direccion,
        Long cuentaId,
        String fechaRegistro) {
}
