package com.bancoxyz.bank_batch_system.bff.web.dto;

public record TransaccionWebDTO(Long id, String fecha, Double monto, String tipo, boolean anomalia) {
}
