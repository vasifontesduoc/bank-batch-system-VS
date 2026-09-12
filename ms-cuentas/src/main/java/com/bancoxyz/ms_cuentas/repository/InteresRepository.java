package com.bancoxyz.ms_cuentas.repository;

import com.bancoxyz.ms_cuentas.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteresRepository extends JpaRepository<Interes, Long> {
    List<Interes> findByCuentaId(Long cuentaId);
}