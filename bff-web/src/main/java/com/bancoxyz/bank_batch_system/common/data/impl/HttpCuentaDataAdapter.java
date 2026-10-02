package com.bancoxyz.bank_batch_system.common.data.impl;

import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.InteresClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.MovimientoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.RetiroResultadoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.TransaccionClienteDTO;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import com.bancoxyz.bank_batch_system.common.exception.CuentaNoEncontradaException;
import com.bancoxyz.bank_batch_system.common.exception.FondosInsuficientesException;
import com.bancoxyz.bank_batch_system.common.exception.ServicioNoDisponibleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
public class HttpCuentaDataAdapter implements CuentaDataPort {

    private static final Logger log = LoggerFactory.getLogger(HttpCuentaDataAdapter.class);
    private static final String CB_NAME = "msCuentas";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public HttpCuentaDataAdapter(RestTemplate restTemplate, ObjectMapper objectMapper,
            @Value("${ms.cuentas.base-url}") String baseUrl,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public EstadoCuentaClienteDTO buscarEstado(Long cuentaId) {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    try {
                        return restTemplate.getForObject(baseUrl + "/internal/cuentas/{cuentaId}/estado",
                                EstadoCuentaClienteDTO.class, cuentaId);
                    } catch (HttpClientErrorException.NotFound ex) {
                        throw new CuentaNoEncontradaException(cuentaId);
                    }
                },
                throwable -> fallback(throwable));
    }

    @Override
    public List<MovimientoClienteDTO> buscarMovimientos(Long cuentaId) {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    MovimientoClienteDTO[] resultado = restTemplate.getForObject(
                            baseUrl + "/internal/cuentas/{cuentaId}/movimientos", MovimientoClienteDTO[].class,
                            cuentaId);
                    return resultado == null ? List.<MovimientoClienteDTO>of() : List.of(resultado);
                },
                throwable -> fallback(throwable));
    }

    @Override
    public List<TransaccionClienteDTO> listarTransacciones() {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    TransaccionClienteDTO[] resultado = restTemplate.getForObject(
                            baseUrl + "/internal/transacciones", TransaccionClienteDTO[].class);
                    return resultado == null ? List.<TransaccionClienteDTO>of() : List.of(resultado);
                },
                throwable -> fallback(throwable));
    }

    @Override
    public List<TransaccionClienteDTO> listarTransaccionesRecientes(int limite) {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    TransaccionClienteDTO[] resultado = restTemplate.getForObject(
                            baseUrl + "/internal/transacciones/recientes?limite={limite}",
                            TransaccionClienteDTO[].class, limite);
                    return resultado == null ? List.<TransaccionClienteDTO>of() : List.of(resultado);
                },
                throwable -> fallback(throwable));
    }

    @Override
    public List<InteresClienteDTO> buscarIntereses(Long cuentaId) {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    InteresClienteDTO[] resultado = restTemplate.getForObject(
                            baseUrl + "/internal/intereses/{cuentaId}", InteresClienteDTO[].class, cuentaId);
                    return resultado == null ? List.<InteresClienteDTO>of() : List.of(resultado);
                },
                throwable -> fallback(throwable));
    }

    @Override
    public RetiroResultadoClienteDTO retirar(Long cuentaId, Double monto) {
        return circuitBreakerFactory.create(CB_NAME).run(
                () -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> request = new HttpEntity<>("{\"monto\": " + monto + "}", headers);
                    try {
                        var response = restTemplate.postForEntity(
                                baseUrl + "/internal/cuentas/{cuentaId}/retiro", request,
                                RetiroResultadoClienteDTO.class, cuentaId);
                        return response.getBody();
                    } catch (HttpClientErrorException.NotFound ex) {
                        throw new CuentaNoEncontradaException(cuentaId);
                    } catch (HttpClientErrorException.Conflict ex) {
                        throw new FondosInsuficientesException(
                                extraerMensaje(ex, "Fondos insuficientes en la cuenta " + cuentaId));
                    }
                },
                throwable -> fallback(throwable));
    }

    private <T> T fallback(Throwable throwable) {
        if (throwable instanceof CuentaNoEncontradaException cuentaEx) {
            throw cuentaEx;
        }
        if (throwable instanceof FondosInsuficientesException fondosEx) {
            throw fondosEx;
        }
        log.error("Circuit breaker activado para ms-cuentas: {}", throwable.getMessage());
        throw new ServicioNoDisponibleException();
    }

    private String extraerMensaje(HttpClientErrorException ex, String fallback) {
        try {
            JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
            return body.has("message") ? body.get("message").asText() : fallback;
        } catch (Exception parseError) {
            log.warn("No se pudo parsear el cuerpo de error de ms-cuentas: {}", parseError.getMessage());
            return fallback;
        }
    }
}