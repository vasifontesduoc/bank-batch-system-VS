package com.bancoxyz.batch_jobs.model;

public class InteresRaw {

    private Long cuentaId;
    private String nombre;
    private String saldo;
    private String edad;
    private String tipo;

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSaldo() { return saldo; }
    public void setSaldo(String saldo) { this.saldo = saldo; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
