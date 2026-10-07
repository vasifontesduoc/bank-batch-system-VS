package com.bancoxyz.batch_jobs.config;

import com.bancoxyz.batch_jobs.batch.intereses.AgregarReporteInteresesTasklet;
import com.bancoxyz.batch_jobs.batch.intereses.InteresItemProcessor;
import com.bancoxyz.batch_jobs.batch.intereses.LimpiarDatosInteresesTasklet;
import com.bancoxyz.batch_jobs.entity.CuentaInteresCalculado;
import com.bancoxyz.batch_jobs.exception.CuentaInteresInvalidaException;
import com.bancoxyz.batch_jobs.model.InteresRaw;
import com.bancoxyz.batch_jobs.repository.CuentaInteresCalculadoRepository;
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
public class InteresesJobConfig {

    @Bean
    public FlatFileItemReader<InteresRaw> interesReader() {
        FlatFileItemReader<InteresRaw> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/intereses_trimestrales.csv"));
        reader.setLinesToSkip(1);
        reader.setLineMapper(interesLineMapper());
        return reader;
    }

    private LineMapper<InteresRaw> interesLineMapper() {
        DefaultLineMapper<InteresRaw> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "nombre", "saldo", "edad", "tipo");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<InteresRaw> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(InteresRaw.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public InteresItemProcessor interesProcessor() {
        return new InteresItemProcessor();
    }

    @Bean
    public ItemWriter<CuentaInteresCalculado> interesWriter(CuentaInteresCalculadoRepository repository) {
        RepositoryItemWriter<CuentaInteresCalculado> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step limpiarDatosInteresesStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager,
                                           JdbcTemplate jdbcTemplate) {
        return new StepBuilder("limpiarDatosInteresesStep", jobRepository)
                .tasklet(new LimpiarDatosInteresesTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Step procesarInteresesStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       ItemReader<InteresRaw> interesReader,
                                       InteresItemProcessor interesProcessor,
                                       ItemWriter<CuentaInteresCalculado> interesWriter) {
        return new StepBuilder("procesarInteresesStep", jobRepository)
                .<InteresRaw, CuentaInteresCalculado>chunk(50, transactionManager)
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .faultTolerant()
                .skip(CuentaInteresInvalidaException.class)
                .skip(FlatFileParseException.class)
                .skipLimit(1000)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step agregarReporteInteresesStep(JobRepository jobRepository,
                                              PlatformTransactionManager transactionManager,
                                              JdbcTemplate jdbcTemplate) {
        return new StepBuilder("agregarReporteInteresesStep", jobRepository)
                .tasklet(new AgregarReporteInteresesTasklet(jdbcTemplate), transactionManager)
                .build();
    }

    @Bean
    public Job calculoInteresesJob(JobRepository jobRepository,
                                    Step limpiarDatosInteresesStep,
                                    Step procesarInteresesStep,
                                    Step agregarReporteInteresesStep) {
        return new JobBuilder("calculoInteresesJob", jobRepository)
                .start(limpiarDatosInteresesStep)
                .next(procesarInteresesStep)
                .next(agregarReporteInteresesStep)
                .build();
    }
}
