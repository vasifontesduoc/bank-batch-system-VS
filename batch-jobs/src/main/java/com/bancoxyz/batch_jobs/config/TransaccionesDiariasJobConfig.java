package com.bancoxyz.batch_jobs.config;

import com.bancoxyz.batch_jobs.batch.transacciones.AgregarReporteTransaccionesTasklet;
import com.bancoxyz.batch_jobs.batch.transacciones.LimpiarDatosTransaccionesTasklet;
import com.bancoxyz.batch_jobs.batch.transacciones.MovimientoItemProcessor;
import com.bancoxyz.batch_jobs.entity.MovimientoProcesado;
import com.bancoxyz.batch_jobs.exception.MovimientoInvalidoException;
import com.bancoxyz.batch_jobs.model.MovimientoDiarioRaw;
import com.bancoxyz.batch_jobs.repository.MovimientoProcesadoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;

@Configuration
public class TransaccionesDiariasJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TransaccionesDiariasJobConfig.class);

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public FlatFileItemReader<MovimientoDiarioRaw> movimientoReader() {
        FlatFileItemReader<MovimientoDiarioRaw> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/movimientos_financieros_diarios.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(movimientoLineMapper());
        return reader;
    }

    private LineMapper<MovimientoDiarioRaw> movimientoLineMapper() {
        DefaultLineMapper<MovimientoDiarioRaw> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "fecha", "monto", "tipo");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<MovimientoDiarioRaw> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(MovimientoDiarioRaw.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public MovimientoItemProcessor movimientoProcessor() {
        return new MovimientoItemProcessor();
    }

    @Bean
    public ItemWriter<MovimientoProcesado> movimientoWriter(MovimientoProcesadoRepository repository) {
        RepositoryItemWriter<MovimientoProcesado> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step limpiarDatosTransaccionesStep(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager,
                                               JdbcTemplate jdbcTemplate) {
        return new StepBuilder("limpiarDatosTransaccionesStep", jobRepository)
                .tasklet(new LimpiarDatosTransaccionesTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Step procesarMovimientosStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         FlatFileItemReader<MovimientoDiarioRaw> movimientoReader,
                                         MovimientoItemProcessor movimientoProcessor,
                                         ItemWriter<MovimientoProcesado> movimientoWriter) {
        // Paralelismo: el reader de archivo no es thread-safe, por eso se desactiva
        // el guardado de estado y se envuelve en un reader sincronizado.
        movimientoReader.setSaveState(false);
        SynchronizedItemStreamReader<MovimientoDiarioRaw> readerSincronizado = new SynchronizedItemStreamReader<>();
        readerSincronizado.setDelegate(movimientoReader);

        // Step multi-hilo: hasta 4 workers procesan chunks en paralelo.
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-worker-");
        executor.setConcurrencyLimit(4);

        return new StepBuilder("procesarMovimientosStep", jobRepository)
                .<MovimientoDiarioRaw, MovimientoProcesado>chunk(50, transactionManager)
                .reader(readerSincronizado)
                .processor(movimientoProcessor)
                .writer(movimientoWriter)
                .faultTolerant()
                .skip(MovimientoInvalidoException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(1000)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .taskExecutor(executor)
                .listener(new ItemWriteListener<MovimientoProcesado>() {
                    @Override
                    public void afterWrite(Chunk<? extends MovimientoProcesado> items) {
                        log.info("PARALELO: chunk de {} registros escrito por el hilo {}",
                                items.size(), Thread.currentThread().getName());
                    }
                })
                .build();
    }

    @Bean
    public Step agregarReporteTransaccionesStep(JobRepository jobRepository,
                                                  PlatformTransactionManager transactionManager,
                                                  JdbcTemplate jdbcTemplate) {
        return new StepBuilder("agregarReporteTransaccionesStep", jobRepository)
                .tasklet(new AgregarReporteTransaccionesTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Job reporteTransaccionesDiariasJob(JobRepository jobRepository,
                                               Step limpiarDatosTransaccionesStep,
                                               Step procesarMovimientosStep,
                                               Step agregarReporteTransaccionesStep) {
        return new JobBuilder("reporteTransaccionesDiariasJob", jobRepository)
                .start(limpiarDatosTransaccionesStep)
                .next(procesarMovimientosStep)
                .next(agregarReporteTransaccionesStep)
                .build();
    }
}
