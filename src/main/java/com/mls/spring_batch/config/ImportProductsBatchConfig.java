package com.mls.spring_batch.config;

import com.mls.spring_batch.decision.FlowDecision;
import com.mls.spring_batch.faultTolerant.CustomSkipPolicy;
import com.mls.spring_batch.listener.ReaderListener;
import com.mls.spring_batch.listener.WriterListener;
import com.mls.spring_batch.model.Product;
import com.mls.spring_batch.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;

@Configuration
@EnableBatchProcessing
public class ImportProductsBatchConfig{

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReaderListener readerListenerStep1;
    private final ProductRepository productRepository;
    private final WriterListener writerListenerStep1;
    private final CustomSkipPolicy customSkipPolicy;
    private final FlowDecision flowDecision;

    public ImportProductsBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                     ReaderListener readerListenerStep1, ProductRepository productRepository,
                                     WriterListener writerListenerStep1, CustomSkipPolicy customSkipPolicy, FlowDecision flowDecision) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.readerListenerStep1 = readerListenerStep1;
        this.productRepository = productRepository;
        this.writerListenerStep1 = writerListenerStep1;
        this.customSkipPolicy = customSkipPolicy;
        this.flowDecision = flowDecision;
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importProducts", jobRepository)
                .start(step1())
                .next(flowDecision)
                .on(FlowExecutionStatus.COMPLETED.toString()).end()
                .on(FlowExecutionStatus.FAILED.toString()).end()
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("importCsvToDatabase", jobRepository)
                .<Product, Product>chunk(10, transactionManager)
                .reader(readerStep1(null))
                .listener(readerListenerStep1)
                .processor(processorStep1())
                .writer(writerStep1())
                .listener(writerListenerStep1)
                .faultTolerant()
                .skipPolicy(customSkipPolicy)
                .build();
    }

    @Bean
    public RepositoryItemWriter<Product> writerStep1() {
        RepositoryItemWriter<Product> writer = new RepositoryItemWriter<>();
        writer.setRepository(productRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public ItemProcessor<Product, Product> processorStep1(){
        return product -> {
            System.out.println("Procesando Product: id=" + product.getId()
                    + " name=" + product.getName()
                    + " price=" + product.getPrice());
            return product;
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Product> readerStep1(@Value("#{jobParameters['inputFile']}") Resource inputResource){
        FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
        reader.setResource(inputResource);
        reader.setLinesToSkip(1);
        reader.setName("csvReader");
        reader.setLineMapper(lineMapper());
        return reader;
    }

    @Bean
    public LineMapper<Product> lineMapper() {
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(false);
        tokenizer.setNames("name", "price");
        tokenizer.setQuoteCharacter(DelimitedLineTokenizer.DEFAULT_QUOTE_CHARACTER);
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<Product> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Product.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
}

