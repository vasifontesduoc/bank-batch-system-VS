package com.bancoxyz.batch_jobs.config;

import com.bancoxyz.batch_jobs.batch.estados.EstadoFinancieroItemProcessor;
import com.bancoxyz.batch_jobs.batch.estados.GenerarEstadosCuentaTasklet;
import com.bancoxyz.batch_jobs.batch.estados.LimpiarDatosEstadosTasklet;
import com.bancoxyz.batch_jobs.entity.MovimientoAnualProcesado;
import com.bancoxyz.batch_jobs.exception.EstadoFinancieroInvalidoException;
import com.bancoxyz.batch_jobs.model.EstadoFinancieroRaw;
import com.bancoxyz.batch_jobs.repository.MovimientoAnualProcesadoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EstadosCuentaJobConfig {

    @Bean
    public FlatFileItemReader<EstadoFinancieroRaw> estadoFinancieroReader() {
        FlatFileItemReader<EstadoFinancieroRaw> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/estados_financieros_anuales.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(estadoFinancieroLineMapper());
        return reader;
    }

    private LineMapper<EstadoFinancieroRaw> estadoFinancieroLineMapper() {
        DefaultLineMapper<EstadoFinancieroRaw> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "fecha", "transaccion", "monto", "descripcion");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<EstadoFinancieroRaw> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(EstadoFinancieroRaw.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public EstadoFinancieroItemProcessor estadoFinancieroProcessor() {
        return new EstadoFinancieroItemProcessor();
    }

    @Bean
    public ItemWriter<MovimientoAnualProcesado> movimientoAnualWriter(MovimientoAnualProcesadoRepository repository) {
        RepositoryItemWriter<MovimientoAnualProcesado> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step limpiarDatosEstadosStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         JdbcTemplate jdbcTemplate) {
        return new StepBuilder("limpiarDatosEstadosStep", jobRepository)
                .tasklet(new LimpiarDatosEstadosTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Step procesarEstadosFinancierosStep(JobRepository jobRepository,
                                                PlatformTransactionManager transactionManager,
                                                ItemReader<EstadoFinancieroRaw> estadoFinancieroReader,
                                                EstadoFinancieroItemProcessor estadoFinancieroProcessor,
                                                ItemWriter<MovimientoAnualProcesado> movimientoAnualWriter) {
        return new StepBuilder("procesarEstadosFinancierosStep", jobRepository)
                .<EstadoFinancieroRaw, MovimientoAnualProcesado>chunk(50, transactionManager)
                .reader(estadoFinancieroReader)
                .processor(estadoFinancieroProcessor)
                .writer(movimientoAnualWriter)
                .faultTolerant()
                .skip(EstadoFinancieroInvalidoException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(1000)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step generarEstadosCuentaStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager,
                                          JdbcTemplate jdbcTemplate) {
        return new StepBuilder("generarEstadosCuentaStep", jobRepository)
                .tasklet(new GenerarEstadosCuentaTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Job generacionEstadosCuentaAnualesJob(JobRepository jobRepository,
                                                  Step limpiarDatosEstadosStep,
                                                  Step procesarEstadosFinancierosStep,
                                                  Step generarEstadosCuentaStep) {
        return new JobBuilder("generacionEstadosCuentaAnualesJob", jobRepository)
                .start(limpiarDatosEstadosStep)
                .next(procesarEstadosFinancierosStep)
                .next(generarEstadosCuentaStep)
                .build();
    }
}
