/*
 * Copyright (C) 2021 Moja Global
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package moja.global.fluxtypes;

import moja.global.fluxtypes.models.FluxType;
import moja.global.fluxtypes.util.builders.FluxTypeBuilder;
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
 * @since 1.0
 * @author Kwaje Anthony <tony@miles.co.ke>
 * @version 1.0
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration(initializers = RetrieveFluxTypesGivenIdsIT.Initializer.class)
public class RetrieveFluxTypesGivenIdsIT {

    @Autowired
    WebTestClient webTestClient;


    static final PostgreSQLContainer postgreSQLContainer;

    static {

        postgreSQLContainer =
                new PostgreSQLContainer("postgres:10.15")
                        .withDatabaseName("units")
                        .withUsername("postgres")
                        .withPassword("postgres");

        postgreSQLContainer
                .withInitScript("init.sql")
                .start();
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
    public void Given_FluxTypeRecordsExist_When_GetAllWithIdsFilter_Then_OnlyFluxTypeRecordsWithTheSpecifiedIdsWillBeReturned() {

        FluxType u1 = new FluxTypeBuilder().id(1L).name("Wildfire").description("Wildfire Flux Type Description").version(1).build();
        FluxType u2 = new FluxTypeBuilder().id(2L).name("Harvest").description("Harvest Flux Type Description").version(1).build();


        webTestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/v1/flux_types/all")
                                .queryParam("ids", "{id1}", "{id2}")
                                .build(u2.getId().toString(), u1.getId().toString()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FluxType.class)
                .value(response -> {

                            Assertions.assertThat(response.get(0).getId() == 1L || response.get(0).getId() == 2L).isTrue();

                            if (response.get(0).getId() == 1L) {
                                Assertions.assertThat(response.get(0).getName())
                                        .isEqualTo(u1.getName());
                                Assertions.assertThat(response.get(0).getDescription())
                                        .isEqualTo(u1.getDescription());
                                Assertions.assertThat(response.get(0).getVersion())
                                        .isEqualTo(u1.getVersion());
                            } else if (response.get(0).getId() == 2L) {
                                Assertions.assertThat(response.get(0).getName())
                                        .isEqualTo(u2.getName());
                                Assertions.assertThat(response.get(0).getDescription())
                                        .isEqualTo(u2.getDescription());
                                Assertions.assertThat(response.get(0).getVersion())
                                        .isEqualTo(u2.getVersion());
                            }

                            Assertions.assertThat(response.get(1).getId() == 1L || response.get(1).getId() == 2L).isTrue();

                            if (response.get(1).getId() == 1L) {
                                Assertions.assertThat(response.get(1).getName())
                                        .isEqualTo(u1.getName());
                                Assertions.assertThat(response.get(1).getDescription())
                                        .isEqualTo(u1.getDescription());
                                Assertions.assertThat(response.get(1).getVersion())
                                        .isEqualTo(u1.getVersion() + 1);
                            } else if (response.get(1).getId() == 2L) {
                                Assertions.assertThat(response.get(1).getName())
                                        .isEqualTo(u2.getName());
                                Assertions.assertThat(response.get(1).getDescription())
                                        .isEqualTo(u2.getDescription());
                                Assertions.assertThat(response.get(1).getVersion())
                                        .isEqualTo(u2.getVersion());
                            }
                        }
                );
    }
}
