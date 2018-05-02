package com.guo.entity;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.Validator;
import org.springframework.batch.support.DatabaseType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
public class CsvBatchConfig2 {
	@Bean
    public ItemReader<Person> reader2(){
        // 使用FlatFileItemReader 读取文件
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("people.csv"));

        reader.setLineMapper(new DefaultLineMapper<Person>(){{
            setLineTokenizer(new DelimitedLineTokenizer(){{
                setNames(new String[]{"name","age","nation","address"});
            }}); 
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>(){{
                setTargetType(Person.class);
            }});
        }});

        return reader;
    }

    @Bean
    public ItemProcessor<Person, Person> processor2(){
        CsvItemProcessor processor = new CsvItemProcessor();
        processor.setValidator(csvBeanValidator2());
        return processor;
    }

    @Bean
    public Validator<Person> csvBeanValidator2(){
        return new CsvBeanValidator<Person>();
    }

    @Bean
    public ItemWriter<Person> writer2(DataSource dataSource){
        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        String sql = "insert into person(name, age, nation, address) "
                                    + "values (:name, :age, :nation, :address)";
        System.out.println("sql为: " +sql);
        writer.setSql(sql);
        writer.setDataSource(dataSource);
        return writer;
    }

    /**
     * 作业仓库
     * 
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */
    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception{

        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setDatabaseType(DatabaseType.MYSQL.name());

        return jobRepositoryFactoryBean.getObject();
    }

    /**
     * 作业调度器
     * 
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */
    public SimpleJobLauncher jobLauncher(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception{

        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(this.jobRepository(dataSource, transactionManager));

        return jobLauncher;
    }

    @Bean
    public Step personStep(StepBuilderFactory stepBuilderFactory, 
                                ItemReader<Person> reader2, ItemWriter<Person> writer2, ItemProcessor<Person, Person> processor2){
        return stepBuilderFactory.get("personStep")
                .<Person, Person>chunk(5000) // 批处理每次提交5000条数据
                .reader(reader2) // 给step绑定reader
                .processor(processor2) // 给step绑定processor
                .writer(writer2) // 给step绑定writer
                .build();
    }
    @Bean
    public Job importJob(JobBuilderFactory jobs, Step step){
        return jobs.get("importJob")
                .incrementer(new RunIdIncrementer())
                .flow(step) // 为Job指定Step
                .end()
                .listener(csvJobListener()) // 绑定监听器
                .build();
    }
    @Bean
    public CsvJobListener csvJobListener(){
        return new CsvJobListener();
    }
}
