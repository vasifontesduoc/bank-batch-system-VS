package com.bancoxyz.bank_batch_system.common.data.impl;

import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.InteresClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.MovimientoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.RetiroResultadoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.TransaccionClienteDTO;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import com.bancoxyz.bank_batch_system.common.exception.CuentaNoEncontradaException;
import com.bancoxyz.bank_batch_system.common.exception.FondosInsuficientesException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Única clase del BFF que sabe que "los datos vienen de ms-cuentas por
 * HTTP". Traduce los códigos de estado del microservicio (404, 409) a las
 * mismas excepciones de negocio que el resto del BFF ya maneja, para que
 * WebService/MobileService/CajeroService no tengan que saber nada sobre
 * HTTP ni sobre el microservicio.
 */
@Component
public class HttpCuentaDataAdapter implements CuentaDataPort {

    private static final Logger log = LoggerFactory.getLogger(HttpCuentaDataAdapter.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public HttpCuentaDataAdapter(RestTemplate restTemplate, ObjectMapper objectMapper,
            @Value("${ms.cuentas.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    @Override
    public EstadoCuentaClienteDTO buscarEstado(Long cuentaId) {
        try {
            return restTemplate.getForObject(baseUrl + "/internal/cuentas/{cuentaId}/estado",
                    EstadoCuentaClienteDTO.class, cuentaId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
    }

    @Override
    public List<MovimientoClienteDTO> buscarMovimientos(Long cuentaId) {
        MovimientoClienteDTO[] resultado = restTemplate.getForObject(
                baseUrl + "/internal/cuentas/{cuentaId}/movimientos", MovimientoClienteDTO[].class, cuentaId);
        return resultado == null ? List.of() : List.of(resultado);
    }

    @Override
    public List<TransaccionClienteDTO> listarTransacciones() {
        TransaccionClienteDTO[] resultado = restTemplate.getForObject(
                baseUrl + "/internal/transacciones", TransaccionClienteDTO[].class);
        return resultado == null ? List.of() : List.of(resultado);
    }

    @Override
    public List<TransaccionClienteDTO> listarTransaccionesRecientes(int limite) {
        TransaccionClienteDTO[] resultado = restTemplate.getForObject(
                baseUrl + "/internal/transacciones/recientes?limite={limite}", TransaccionClienteDTO[].class, limite);
        return resultado == null ? List.of() : List.of(resultado);
    }

    @Override
    public List<InteresClienteDTO> buscarIntereses(Long cuentaId) {
        InteresClienteDTO[] resultado = restTemplate.getForObject(
                baseUrl + "/internal/intereses/{cuentaId}", InteresClienteDTO[].class, cuentaId);
        return resultado == null ? List.of() : List.of(resultado);
    }

    @Override
    public RetiroResultadoClienteDTO retirar(Long cuentaId, Double monto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"monto\": " + monto + "}", headers);

        try {
            var response = restTemplate.postForEntity(
                    baseUrl + "/internal/cuentas/{cuentaId}/retiro", request, RetiroResultadoClienteDTO.class,
                    cuentaId);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(cuentaId);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new FondosInsuficientesException(extraerMensaje(ex, "Fondos insuficientes en la cuenta " + cuentaId));
        }
    }

    /**
     * Extrae el campo "message" del cuerpo de error uniforme que devuelve
     * ms-cuentas.
     */
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