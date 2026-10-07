package com.bancoxyz.batch_jobs.batch.transacciones;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimpiarDatosTransaccionesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public LimpiarDatosTransaccionesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute("DELETE FROM reporte_transacciones_diarias");
        jdbcTemplate.execute("DELETE FROM movimientos_procesados");
        return RepeatStatus.FINISHED;
    }
}
