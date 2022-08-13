/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.config;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.TimeZone;
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
        try {
            var ds = new MysqlDataSource();

            ds.setCharacterEncoding("utf8");
            ds.setUseSSL(false);
            ds.setServerTimezone(TimeZone.getDefault().getID());
            ds.setPort(properties.port());
            ds.setServerName(properties.host());
            ds.setUser(properties.userName());
            ds.setPassword(properties.password());
            ds.setDatabaseName(properties.schema());
            ds.setAllowPublicKeyRetrieval(true);
            return ds;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
