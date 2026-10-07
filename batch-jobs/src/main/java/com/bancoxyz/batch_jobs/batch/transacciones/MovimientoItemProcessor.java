package com.bancoxyz.batch_jobs.batch.transacciones;

import com.bancoxyz.batch_jobs.entity.MovimientoProcesado;
import com.bancoxyz.batch_jobs.exception.MovimientoInvalidoException;
import com.bancoxyz.batch_jobs.model.MovimientoDiarioRaw;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MovimientoItemProcessor implements ItemProcessor<MovimientoDiarioRaw, MovimientoProcesado> {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private static final List<String> TIPOS_VALIDOS = List.of("credito", "debito");

    @Override
    public MovimientoProcesado process(MovimientoDiarioRaw raw) {
        String tipo = raw.getTipo() == null ? "" : raw.getTipo().trim().toLowerCase();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new MovimientoInvalidoException(
                    "Tipo de transacción inválido en movimiento id=" + raw.getId() + ": '" + raw.getTipo() + "'");
        }

        if (raw.getMonto() == null || raw.getMonto().isBlank()) {
            throw new MovimientoInvalidoException(
                    "Monto vacío en movimiento id=" + raw.getId());
        }
        BigDecimal monto;
        try {
            monto = new BigDecimal(raw.getMonto().trim());
        } catch (NumberFormatException e) {
            throw new MovimientoInvalidoException(
                    "Monto no numérico en movimiento id=" + raw.getId() + ": '" + raw.getMonto() + "'");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MovimientoInvalidoException(
                    "Monto no positivo en movimiento id=" + raw.getId() + ": " + monto);
        }

        LocalDate fecha = parsearFecha(raw.getId(), raw.getFecha());

        return new MovimientoProcesado(fecha, monto, tipo);
    }

    private LocalDate parsearFecha(Long id, String fechaTexto) {
        if (fechaTexto == null || fechaTexto.isBlank()) {
            throw new MovimientoInvalidoException("Fecha vacía en movimiento id=" + id);
        }
        for (DateTimeFormatter formato : FORMATOS) {
            try {
                return LocalDate.parse(fechaTexto.trim(), formato);
            } catch (DateTimeParseException ignored) {
                // intenta el siguiente formato
            }
        }
        throw new MovimientoInvalidoException(
                "Formato de fecha no reconocido en movimiento id=" + id + ": '" + fechaTexto + "'");
    }
}
