package com.bancoxyz.batch_jobs.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "intereses_calculados")
public class CuentaInteresCalculado {

    @Id
    private Long cuentaId;

    private String nombre;
    private BigDecimal saldo;
    private String tipoCuenta;
    private BigDecimal interesCalculado;
    private LocalDate fechaCalculo;

    public CuentaInteresCalculado() {}

    public CuentaInteresCalculado(Long cuentaId, String nombre, BigDecimal saldo,
                                   String tipoCuenta, BigDecimal interesCalculado, LocalDate fechaCalculo) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
        this.interesCalculado = interesCalculado;
        this.fechaCalculo = fechaCalculo;
    }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public BigDecimal getInteresCalculado() { return interesCalculado; }
    public void setInteresCalculado(BigDecimal interesCalculado) { this.interesCalculado = interesCalculado; }

    public LocalDate getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDate fechaCalculo) { this.fechaCalculo = fechaCalculo; }
}
