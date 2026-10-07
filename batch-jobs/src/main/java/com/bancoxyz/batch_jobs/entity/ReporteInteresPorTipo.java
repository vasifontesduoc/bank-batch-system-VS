package com.bancoxyz.batch_jobs.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "reporte_intereses_por_tipo")
public class ReporteInteresPorTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoCuenta;
    private Long cantidadCuentas;
    private BigDecimal interesTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public Long getCantidadCuentas() { return cantidadCuentas; }
    public void setCantidadCuentas(Long c) { this.cantidadCuentas = c; }

    public BigDecimal getInteresTotal() { return interesTotal; }
    public void setInteresTotal(BigDecimal i) { this.interesTotal = i; }
}
