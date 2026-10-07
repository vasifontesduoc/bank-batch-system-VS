package com.bancoxyz.ms_clientes.service;

import com.bancoxyz.ms_clientes.client.CuentasClient;
import com.bancoxyz.ms_clientes.dto.*;
import com.bancoxyz.ms_clientes.model.Cliente;
import com.bancoxyz.ms_clientes.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final CuentasClient cuentasClient;

    public ClienteService(ClienteRepository repository, CuentasClient cuentasClient) {
        this.repository = repository;
        this.cuentasClient = cuentasClient;
    }

    public ClienteResponse crear(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
        cliente.setCuentaId(request.cuentaId());
        cliente.setFechaRegistro(LocalDate.now());
        return toResponse(repository.save(cliente));
    }

    public ClienteResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
        cliente.setCuentaId(request.cuentaId());
        return toResponse(repository.save(cliente));
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    public ResumenFinancieroResponse resumenFinanciero(Long id) {
        Cliente cliente = buscar(id);
        if (cliente.getCuentaId() == null) {
            return new ResumenFinancieroResponse(cliente.getId(), null, null, null, false,
                    "El cliente no tiene una cuenta asociada");
        }

        EstadoCuentaResponse estado = cuentasClient.obtenerEstado(cliente.getCuentaId());
        boolean disponible = estado.fechaGeneracion() != null;

        return new ResumenFinancieroResponse(
                cliente.getId(),
                cliente.getCuentaId(),
                estado.saldoFinal(),
                estado.cantidadMovimientos(),
                disponible,
                disponible ? "OK" : "ms-cuentas no disponible en este momento, mostrando datos por defecto"
        );
    }

    private Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getId(), c.getNombre(), c.getEmail(), c.getTelefono(), c.getDireccion(),
                c.getCuentaId(), c.getFechaRegistro() == null ? null : c.getFechaRegistro().toString());
    }
}
