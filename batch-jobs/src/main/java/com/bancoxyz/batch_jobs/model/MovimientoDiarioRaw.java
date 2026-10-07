package com.bancoxyz.batch_jobs.model;

public class MovimientoDiarioRaw {

    private Long id;
    private String fecha;
    private String monto;
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
