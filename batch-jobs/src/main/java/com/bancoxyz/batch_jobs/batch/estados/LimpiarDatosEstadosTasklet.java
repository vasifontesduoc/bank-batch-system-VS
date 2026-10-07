package com.bancoxyz.batch_jobs.batch.estados;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimpiarDatosEstadosTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public LimpiarDatosEstadosTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // Solo limpia la tabla de staging propia de este job.
        // NO toca estados_cuenta_anuales: esa tabla es compartida con ms-cuentas
        // y se actualiza vía upsert (ON DUPLICATE KEY UPDATE) en el paso de agregación.
        jdbcTemplate.execute("DELETE FROM movimientos_anuales_procesados");
        return RepeatStatus.FINISHED;
    }
}
