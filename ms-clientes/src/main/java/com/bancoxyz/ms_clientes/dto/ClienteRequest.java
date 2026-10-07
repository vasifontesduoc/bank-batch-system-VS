package com.bancoxyz.ms_clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank @Email(message = "El email no es válido") String email,
        String telefono,
        String direccion,
        Long cuentaId) {
}
