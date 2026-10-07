package com.bancoxyz.batch_jobs.batch.transacciones;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class AgregarReporteTransaccionesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public AgregarReporteTransaccionesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute(
                "INSERT INTO reporte_transacciones_diarias (fecha, tipo, cantidad_transacciones, monto_total) " +
                "SELECT fecha, tipo, COUNT(*), SUM(monto) " +
                "FROM movimientos_procesados " +
                "GROUP BY fecha, tipo"
        );
        return RepeatStatus.FINISHED;
    }
}
