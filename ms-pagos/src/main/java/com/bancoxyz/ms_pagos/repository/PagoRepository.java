package com.bancoxyz.ms_pagos.repository;

import com.bancoxyz.ms_pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
}
