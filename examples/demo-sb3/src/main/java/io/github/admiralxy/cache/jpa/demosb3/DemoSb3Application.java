package io.github.admiralxy.cache.jpa.demosb3;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@RequiredArgsConstructor
@SpringBootApplication
public class DemoSb3Application implements CommandLineRunner {

    private final CacheTest cacheTest;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoSb3Application.class, args);
        SpringApplication.exit(context);
    }

    @Override
    public void run(String... args) {
        cacheTest.getValueWithJpaCacheCheck("1");
        cacheTest.getValueWithJpaCacheCheck("1");
        cacheTest.getValueWithJpaCacheCheck("1");

        cacheTest.getValueWithJpaCacheCheck("2");
        cacheTest.getValueWithJpaCacheCheck("2");
        cacheTest.getValueWithJpaCacheCheck("2");

        cacheTest.getValueWithJpaCacheCheck("3");
        cacheTest.getValueWithJpaCacheCheck("4");
        cacheTest.getValueWithJpaCacheCheck("5");
        cacheTest.getValueWithJpaCacheCheck("6");

        cacheTest.getValueWithInMemoryCacheCheck("100");
        cacheTest.getValueWithInMemoryCacheCheck("100");
        cacheTest.getValueWithInMemoryCacheCheck("100");

        cacheTest.getValueWithInMemoryCacheCheck("101");
        cacheTest.getValueWithInMemoryCacheCheck("102");
        cacheTest.getValueWithInMemoryCacheCheck("103");
    }
}
