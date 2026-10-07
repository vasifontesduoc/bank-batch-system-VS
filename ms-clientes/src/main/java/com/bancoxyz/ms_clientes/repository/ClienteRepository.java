package com.bancoxyz.ms_clientes.repository;

import com.bancoxyz.ms_clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
