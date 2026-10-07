package com.bancoxyz.batch_jobs.batch.estados;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class GenerarEstadosCuentaTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public GenerarEstadosCuentaTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        jdbcTemplate.execute(
                "INSERT INTO estados_cuenta_anuales " +
                "(cuenta_id, cantidad_movimientos, total_depositos, total_retiros, total_otros, saldo_final, fecha_generacion) " +
                "SELECT cuenta_id, " +
                "       COUNT(*), " +
                "       SUM(CASE WHEN transaccion = 'deposito' THEN monto ELSE 0 END), " +
                "       SUM(CASE WHEN transaccion = 'retiro' THEN monto ELSE 0 END), " +
                "       SUM(CASE WHEN transaccion NOT IN ('deposito', 'retiro') THEN monto ELSE 0 END), " +
                "       SUM(CASE WHEN transaccion = 'deposito' THEN monto ELSE -monto END), " +
                "       CURDATE() " +
                "FROM movimientos_anuales_procesados " +
                "GROUP BY cuenta_id " +
                "ON DUPLICATE KEY UPDATE " +
                "  cantidad_movimientos = VALUES(cantidad_movimientos), " +
                "  total_depositos = VALUES(total_depositos), " +
                "  total_retiros = VALUES(total_retiros), " +
                "  total_otros = VALUES(total_otros), " +
                "  saldo_final = VALUES(saldo_final), " +
                "  fecha_generacion = VALUES(fecha_generacion)"
        );
        return RepeatStatus.FINISHED;
    }
}
