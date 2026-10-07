package com.bancoxyz.batch_jobs.batch.estados;

import com.bancoxyz.batch_jobs.entity.MovimientoAnualProcesado;
import com.bancoxyz.batch_jobs.exception.EstadoFinancieroInvalidoException;
import com.bancoxyz.batch_jobs.model.EstadoFinancieroRaw;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EstadoFinancieroItemProcessor implements ItemProcessor<EstadoFinancieroRaw, MovimientoAnualProcesado> {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    @Override
    public MovimientoAnualProcesado process(EstadoFinancieroRaw raw) {
        Long cuentaId = raw.getCuentaId();
        if (cuentaId == null) {
            throw new EstadoFinancieroInvalidoException("Registro sin cuenta_id");
        }

        if (raw.getDescripcion() == null || raw.getDescripcion().isBlank()) {
            throw new EstadoFinancieroInvalidoException("Descripción vacía en cuenta_id=" + cuentaId);
        }

        if (raw.getMonto() == null || raw.getMonto().isBlank()) {
            throw new EstadoFinancieroInvalidoException("Monto vacío en cuenta_id=" + cuentaId);
        }
        BigDecimal monto;
        try {
            monto = new BigDecimal(raw.getMonto().trim());
        } catch (NumberFormatException e) {
            throw new EstadoFinancieroInvalidoException(
                    "Monto no numérico en cuenta_id=" + cuentaId + ": '" + raw.getMonto() + "'");
        }
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new EstadoFinancieroInvalidoException("Monto negativo en cuenta_id=" + cuentaId + ": " + monto);
        }

        LocalDate fecha = parsearFecha(cuentaId, raw.getFecha());

        String transaccion = raw.getTransaccion() == null ? "otros" : raw.getTransaccion().trim().toLowerCase();

        return new MovimientoAnualProcesado(cuentaId, fecha, transaccion, monto, raw.getDescripcion().trim());
    }

    private LocalDate parsearFecha(Long cuentaId, String fechaTexto) {
        if (fechaTexto == null || fechaTexto.isBlank()) {
            throw new EstadoFinancieroInvalidoException("Fecha vacía en cuenta_id=" + cuentaId);
        }
        for (DateTimeFormatter formato : FORMATOS) {
            try {
                return LocalDate.parse(fechaTexto.trim(), formato);
            } catch (DateTimeParseException ignored) {
                // intenta el siguiente formato
            }
        }
        throw new EstadoFinancieroInvalidoException(
                "Formato de fecha no reconocido en cuenta_id=" + cuentaId + ": '" + fechaTexto + "'");
    }
}
