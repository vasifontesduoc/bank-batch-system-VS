package com.bancoxyz.ms_cuentas.repository;

import com.bancoxyz.ms_cuentas.model.CuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaAnualRepository extends JpaRepository<CuentaAnual, Long> {
    List<CuentaAnual> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}