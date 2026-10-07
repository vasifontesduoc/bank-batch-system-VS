package com.bancoxyz.batch_jobs.batch.intereses;

import com.bancoxyz.batch_jobs.entity.CuentaInteresCalculado;
import com.bancoxyz.batch_jobs.exception.CuentaInteresInvalidaException;
import com.bancoxyz.batch_jobs.model.InteresRaw;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

public class InteresItemProcessor implements ItemProcessor<InteresRaw, CuentaInteresCalculado> {

    private static final Map<String, BigDecimal> TASAS_POR_TIPO = Map.of(
            "ahorro", new BigDecimal("0.02"),
            "hipoteca", new BigDecimal("0.04"),
            "prestamo", new BigDecimal("0.06")
    );

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 100;

    @Override
    public CuentaInteresCalculado process(InteresRaw raw) {
        Long cuentaId = raw.getCuentaId();
        if (cuentaId == null) {
            throw new CuentaInteresInvalidaException("Registro sin cuenta_id");
        }

        String tipo = raw.getTipo() == null ? "" : raw.getTipo().trim().toLowerCase();
        if (!TASAS_POR_TIPO.containsKey(tipo)) {
            throw new CuentaInteresInvalidaException(
                    "Tipo de cuenta inválido en cuenta_id=" + cuentaId + ": '" + raw.getTipo() + "'");
        }

        if (raw.getSaldo() == null || raw.getSaldo().isBlank()) {
            throw new CuentaInteresInvalidaException("Saldo vacío en cuenta_id=" + cuentaId);
        }
        BigDecimal saldo;
        try {
            saldo = new BigDecimal(raw.getSaldo().trim());
        } catch (NumberFormatException e) {
            throw new CuentaInteresInvalidaException(
                    "Saldo no numérico en cuenta_id=" + cuentaId + ": '" + raw.getSaldo() + "'");
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new CuentaInteresInvalidaException("Saldo negativo en cuenta_id=" + cuentaId);
        }

        if (raw.getEdad() == null || raw.getEdad().isBlank()) {
            throw new CuentaInteresInvalidaException("Edad vacía en cuenta_id=" + cuentaId);
        }
        int edad;
        try {
            edad = Integer.parseInt(raw.getEdad().trim());
        } catch (NumberFormatException e) {
            throw new CuentaInteresInvalidaException(
                    "Edad no numérica en cuenta_id=" + cuentaId + ": '" + raw.getEdad() + "'");
        }
        if (edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            throw new CuentaInteresInvalidaException(
                    "Edad fuera de rango en cuenta_id=" + cuentaId + ": " + edad);
        }

        BigDecimal tasa = TASAS_POR_TIPO.get(tipo);
        BigDecimal interes = saldo.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        return new CuentaInteresCalculado(cuentaId, raw.getNombre(), saldo, tipo, interes, LocalDate.now());
    }
}
