package com.bancoxyz.batch_jobs.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reporte_transacciones_diarias")
public class ReporteTransaccionDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private String tipo;
    private Long cantidadTransacciones;
    private BigDecimal montoTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getCantidadTransacciones() { return cantidadTransacciones; }
    public void setCantidadTransacciones(Long c) { this.cantidadTransacciones = c; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal m) { this.montoTotal = m; }
}
