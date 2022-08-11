/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

import static org.panteleyev.money.backend.Profiles.NOT;
import static org.panteleyev.money.backend.Profiles.TEST;

@Configuration
public class JdbcConfiguration {
    private final DatabaseProperties properties;

    public JdbcConfiguration(DatabaseProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Profile(NOT + TEST)
    public DataSource mysqlDataSource() {
        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{properties.host()});
        ds.setPortNumbers(new int[]{properties.port()});
        ds.setUser(properties.userName());
        ds.setPassword(properties.password());
        ds.setDatabaseName(properties.name());
        ds.setCurrentSchema(properties.schema());
        return ds;
    }
}
