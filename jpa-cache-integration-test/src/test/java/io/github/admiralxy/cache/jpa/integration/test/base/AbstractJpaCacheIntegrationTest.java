package io.github.admiralxy.cache.jpa.integration.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.admiralxy.cache.jpa.api.CacheSerializer;
import io.github.admiralxy.cache.jpa.api.entity.entry.JpaCacheEntity;
import io.github.admiralxy.cache.jpa.api.entity.leader.JpaCacheLeaderEntity;
import io.github.admiralxy.cache.jpa.core.JpaCacheManager;
import io.github.admiralxy.cache.jpa.core.JpaCacheSettings;
import io.github.admiralxy.cache.jpa.core.serializer.JacksonSerializer;
import io.github.admiralxy.cache.jpa.core.task.LeaderTtlCleaner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnabledIfSystemProperty(
        named = "jdbc.test.db",
        matches = "(?i)postgres|oracle|mssql",
        disabledReason = "jdbc.test.db property not set or unsupported"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJpaCacheIntegrationTest {

    private static final Map<String, JdbcDatabaseContainer<?>> CONTAINERS = Map.of(
            "postgres", new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("postgres")
                    .withUsername("test")
                    .withPassword("test"),
            "oracle", new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:18-slim"))
                    .withDatabaseName("oracle")
                    .withUsername("test")
                    .withPassword("test"),
            "mssql", new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-CU15-ubuntu-20.04"))
                    .withUrlParam("databaseName","mssql")
                    .withUrlParam("encrypt","false")
                    .acceptLicense()
    );

    private static final Map<String, String> DIALECTS = Map.of(
            "postgres", "org.hibernate.dialect.PostgreSQLDialect",
            "oracle", "org.hibernate.dialect.Oracle10gDialect",
            "mssql", "org.hibernate.dialect.SQLServerDialect"
    );

    private static JdbcDatabaseContainer<?> container;
    protected AnnotationConfigApplicationContext ctx;

    @BeforeAll
    void setupContext() {
        String db = System.getProperty("jdbc.test.db");
        Assumptions.assumeTrue(
                db != null && CONTAINERS.containsKey(db.toLowerCase()),
                () -> "Skipping integration tests: 'jdbc.test.db' property not set or unsupported"
        );
        container = CONTAINERS.get(db.toLowerCase());
        container.start();

        ctx = new AnnotationConfigApplicationContext();
        DefaultListableBeanFactory bf = ctx.getDefaultListableBeanFactory();
        bf.setAllowBeanDefinitionOverriding(true);
        ctx.register(SharedConfig.class, getTestConfigClass());
        ctx.refresh();
    }

    @AfterAll
    void tearDown() {
        if (ctx != null) {
            ctx.close();
        }
        if (container != null) {
            container.stop();
        }
    }

    protected JpaCacheManager cacheManager() {
        return ctx.getBean(JpaCacheManager.class);
    }

    protected abstract Class<?> getTestConfigClass();

    @Configuration
    @EnableTransactionManagement
    static class SharedConfig {

        @Bean
        DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(container.getDriverClassName());
            ds.setUrl(container.getJdbcUrl());
            ds.setUsername(container.getUsername());
            ds.setPassword(container.getPassword());
            return ds;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setDataSource(ds);
            emf.setPackagesToScan(
                    JpaCacheEntity.class.getPackageName(),
                    JpaCacheLeaderEntity.class.getPackageName()
            );
            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            emf.setJpaVendorAdapter(adapter);

            Map<String,Object> props = new HashMap<>();
            props.put("hibernate.dialect", resolveHibernateDialect(container));
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");
            emf.setJpaPropertyMap(props);
            return emf;
        }

        @Bean
        public SharedEntityManagerBean entityManager(EntityManagerFactory emf) {
            SharedEntityManagerBean em = new SharedEntityManagerBean();
            em.setEntityManagerFactory(emf);
            return em;
        }

        private String resolveHibernateDialect(JdbcDatabaseContainer<?> c) {
            String product = c.getDatabaseName().toLowerCase();
            return DIALECTS.get(product);
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        TransactionTemplate txTemplate(PlatformTransactionManager tm) {
            return new TransactionTemplate(tm);
        }

        @Bean
        SpringLiquibase liquibase(DataSource ds) {
            SpringLiquibase liq = new SpringLiquibase();
            liq.setDataSource(ds);
            liq.setChangeLog("classpath:db/changelog/jdbc-cache-changelog.yaml");
            return liq;
        }

        @Bean
        CacheSerializer serializer() {
            return new JacksonSerializer(new ObjectMapper());
        }

        @Bean
        JpaCacheManager cacheManager(EntityManager em,
                                     PlatformTransactionManager tm,
                                     CacheSerializer ser,
                                     JpaCacheSettings settings) {
            return new JpaCacheManager(em, tm, ser, settings);
        }

        @Bean(destroyMethod = "close")
        LeaderTtlCleaner leaderTtlCleaner(EntityManager em,
                                          PlatformTransactionManager tm,
                                          JpaCacheManager mgr,
                                          JpaCacheSettings settings) {
            return new LeaderTtlCleaner(em, tm, mgr, settings);
        }
    }
}
