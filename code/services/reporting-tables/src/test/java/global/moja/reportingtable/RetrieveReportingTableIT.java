/*
 * Copyright (C) 2021 Moja Global
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package global.moja.reportingtable;

import global.moja.reportingtable.models.ReportingTable;
import global.moja.reportingtable.util.builders.ReportingTableBuilder;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Kwaje Anthony <tony@miles.co.ke>
 * @version 1.0
 * @since 1.0
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = RetrieveReportingTableIT.Initializer.class)
public class RetrieveReportingTableIT {

    @Autowired
    WebTestClient webTestClient;


    static final PostgreSQLContainer postgreSQLContainer;
    static final ReportingTable reportingTable1;

    static {

        postgreSQLContainer =
                new PostgreSQLContainer("postgres:10.15")
                        .withDatabaseName("reportingTables")
                        .withUsername("postgres")
                        .withPassword("postgres");

        postgreSQLContainer
                .withInitScript("init.sql")
                .start();

        reportingTable1 =
                new ReportingTableBuilder()
                        .id(1L)
                        .reportingFrameworkId(1L)
                        .number("First")
                        .name("First ReportingTable")
                        .description(null)
                        .version(1)
                        .build();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername()
            );
            values.applyTo(configurableApplicationContext);
        }
    }

    @AfterClass
    public static void shutdown() {

        postgreSQLContainer.stop();
    }

    @Test
    public void Given_ReportingTableRecordExists_When_GetWithIdParameter_Then_TheReportingTableRecordWithThatIdWillBeReturned() {

        webTestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/v1/reporting_tables/ids/{id}")
                                .build(Long.toString(reportingTable1.getId())))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReportingTable.class)
                .value(response -> {
                    Assertions.assertThat(response.getId()).isEqualTo(reportingTable1.getId());
                    Assertions.assertThat(response.getReportingFrameworkId()).isEqualTo(reportingTable1.getReportingFrameworkId());
                    Assertions.assertThat(response.getNumber()).isEqualTo(reportingTable1.getNumber());
                    Assertions.assertThat(response.getName()).isEqualTo(reportingTable1.getName());
                    Assertions.assertThat(response.getDescription()).isEqualTo(reportingTable1.getDescription());
                    Assertions.assertThat(response.getVersion()).isEqualTo(reportingTable1.getVersion());
                        }
                );
    }
}
