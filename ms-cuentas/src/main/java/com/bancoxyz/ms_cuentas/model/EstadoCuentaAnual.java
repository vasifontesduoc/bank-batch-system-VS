package com.bancoxyz.ms_cuentas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Estado de cuenta anual COMPILADO para una cuenta: es el resultado de
 * agregar todos sus movimientos del año.
 */
@Entity
@Table(name = "estados_cuenta_anuales")
public class EstadoCuentaAnual {

    @Id
    private Long cuentaId;

    private Integer cantidadMovimientos;
    private Double totalDepositos;
    private Double totalRetiros;
    private Double totalOtros;
    private Double saldoFinal;
    private String fechaGeneracion;

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public Integer getCantidadMovimientos() {
        return cantidadMovimientos;
    }

    public void setCantidadMovimientos(Integer cantidadMovimientos) {
        this.cantidadMovimientos = cantidadMovimientos;
    }

    public Double getTotalDepositos() {
        return totalDepositos;
    }

    public void setTotalDepositos(Double totalDepositos) {
        this.totalDepositos = totalDepositos;
    }

    public Double getTotalRetiros() {
        return totalRetiros;
    }

    public void setTotalRetiros(Double totalRetiros) {
        this.totalRetiros = totalRetiros;
    }

    public Double getTotalOtros() {
        return totalOtros;
    }

    public void setTotalOtros(Double totalOtros) {
        this.totalOtros = totalOtros;
    }

    public Double getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(Double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
}