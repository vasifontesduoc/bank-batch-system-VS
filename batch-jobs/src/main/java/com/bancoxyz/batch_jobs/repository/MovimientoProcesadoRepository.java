package com.bancoxyz.batch_jobs.repository;

import com.bancoxyz.batch_jobs.entity.MovimientoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoProcesadoRepository extends JpaRepository<MovimientoProcesado, Long> {
}
