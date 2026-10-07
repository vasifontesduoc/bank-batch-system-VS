package com.bancoxyz.batch_jobs.model;

public class EstadoFinancieroRaw {

    private Long cuentaId;
    private String fecha;
    private String transaccion;
    private String monto;
    private String descripcion;

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTransaccion() { return transaccion; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
