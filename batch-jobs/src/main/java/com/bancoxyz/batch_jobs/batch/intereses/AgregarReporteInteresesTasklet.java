package com.bancoxyz.batch_jobs.batch.intereses;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class AgregarReporteInteresesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public AgregarReporteInteresesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute(
                "INSERT INTO reporte_intereses_por_tipo (tipo_cuenta, cantidad_cuentas, interes_total) " +
                "SELECT tipo_cuenta, COUNT(*), SUM(interes_calculado) " +
                "FROM intereses_calculados " +
                "GROUP BY tipo_cuenta"
        );
        return RepeatStatus.FINISHED;
    }
}
