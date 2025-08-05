package org.myblog.users.unit.service;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1")
                .withDatabaseName("myblog-test")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.start();

        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.jpa.hibernate.ddl-auto=create-drop"
        ).applyTo(context.getEnvironment());

        context.getBeanFactory()
                .registerSingleton("postgresTestContainer", postgres);
    }
}
