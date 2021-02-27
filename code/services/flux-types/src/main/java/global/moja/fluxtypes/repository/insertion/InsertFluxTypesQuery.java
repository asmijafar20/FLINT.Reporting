/*
 * Copyright (C) 2021 Moja Global
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package global.moja.fluxtypes.repository.insertion;

import global.moja.fluxtypes.models.FluxType;
import io.reactivex.Flowable;
import global.moja.fluxtypes.configurations.DatabaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kwaje Anthony <tony@miles.co.ke>
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class InsertFluxTypesQuery {

    @Autowired
    DatabaseConfig databaseConfig;

    /**
     * Inserts new Flux Types records into the database
     *
     * @param fluxTypes an array of beans containing the Flux Types records details
     * @return the unique identifiers of the newly inserted Flux Types records
     */
    public Flux<Long> insertFluxTypes(FluxType[] fluxTypes) {

        log.trace("Entering insertFluxTypes");

        String query = "INSERT INTO flux_type(name,description) VALUES(?,?)";

        return
                Flux.from(
                        databaseConfig
                                .getDatabase()
                                .update(query)
                                .parameterListStream(getParametersListStream(fluxTypes))
                                .returnGeneratedKeys()
                                .getAs(Long.class));
    }

    private Flowable getParametersListStream(FluxType[] fluxTypes) {

        List<List> temp = new ArrayList<>();

        for (FluxType fluxType : fluxTypes) {
            temp.add(Arrays.asList(
                    fluxType.getName(),
                    fluxType.getDescription()
            ));
        }

        return Flowable.fromIterable(temp);
    }

}
