package com.bancoxyz.ms_cuentas.repository;

import com.bancoxyz.ms_cuentas.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}
