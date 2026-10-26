package cl.duoc.libros;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.config.location=classpath:/application.properties");

    @Test
    void loadsDotEnvAndResolvesDatasourceProperties() {
        runner.run(context -> {
            var environment = context.getEnvironment();
            assertThat(environment.getPropertySources().stream()
                    .anyMatch(source -> source.getName().contains(".env"))).isTrue();
            for (String[] mapping : new String[][] {
                    {"spring.datasource.url", "ORACLE_URL"},
                    {"spring.datasource.username", "ORACLE_USERNAME"},
                    {"spring.datasource.password", "ORACLE_PASSWORD"}}) {
                assertThat(environment.getProperty(mapping[1])).isNotBlank();
                assertThat(environment.getProperty(mapping[0]))
                        .isEqualTo(environment.getProperty(mapping[1]));
            }
        });
    }

    @Test
    void environmentVariablesOverrideDotEnvValues() {
        runner.withInitializer(context -> context.getEnvironment().getPropertySources()
                .addFirst(new SystemEnvironmentPropertySource("testEnvironment", Map.of(
                        "ORACLE_USERNAME", "external_user",
                        "ORACLE_PASSWORD", "external_dummy_password",
                        "ORACLE_URL", "jdbc:oracle:thin:@external_alias"))))
                .run(context -> {
                    var environment = context.getEnvironment();
                    assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("external_user");
                    assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("external_dummy_password");
                    assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:oracle:thin:@external_alias");
                });
    }
}
