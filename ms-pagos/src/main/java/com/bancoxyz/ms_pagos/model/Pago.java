package com.bancoxyz.ms_pagos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaOrigenId;
    private Double monto;
    private String descripcion;
    private String estado;
    private Double saldoAnterior;
    private Double saldoNuevo;
    private LocalDateTime fecha;

    public Pago() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getSaldoAnterior() { return saldoAnterior; }
    public void setSaldoAnterior(Double saldoAnterior) { this.saldoAnterior = saldoAnterior; }

    public Double getSaldoNuevo() { return saldoNuevo; }
    public void setSaldoNuevo(Double saldoNuevo) { this.saldoNuevo = saldoNuevo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
