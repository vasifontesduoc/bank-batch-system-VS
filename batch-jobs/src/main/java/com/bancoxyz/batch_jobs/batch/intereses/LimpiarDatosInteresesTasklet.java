package com.bancoxyz.batch_jobs.batch.intereses;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimpiarDatosInteresesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public LimpiarDatosInteresesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute("DELETE FROM reporte_intereses_por_tipo");
        jdbcTemplate.execute("DELETE FROM intereses_calculados");
        return RepeatStatus.FINISHED;
    }
}
