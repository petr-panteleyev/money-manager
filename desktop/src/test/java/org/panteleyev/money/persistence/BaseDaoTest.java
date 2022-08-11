/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import javafx.embed.swing.JFXPanel;
import org.panteleyev.money.test.BaseTest;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.panteleyev.money.app.GlobalContext.dao;

public class BaseDaoTest extends BaseTest {
    private final static Logger LOGGER = Logger.getLogger(BaseDaoTest.class.getName());

    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    private static PostgreSQLContainer<?> container = null;

    public void setupAndSkip() throws Exception {
        try {
            container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.5"))
                    .withLogConsumer(outputFrame -> {
                        var logMessage = outputFrame.getUtf8String();
                        switch (outputFrame.getType()) {
                            case STDOUT -> LOGGER.info(logMessage);
                            case STDERR -> LOGGER.severe(logMessage);
                        }
                    })
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");
            container.start();

            var dataSource = new PGSimpleDataSource();
            dataSource.setURL(container.getJdbcUrl());
            dataSource.setUser(container.getUsername());
            dataSource.setPassword(container.getPassword());

            try (var conn = dataSource.getConnection()) {
                new LiquibaseUtil(conn).update();
            }
            dao().initialize(dataSource);
            new JFXPanel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tearDown() throws Exception {
        if (container != null && container.isRunning()) {
            container.close();
            container.stop();
        }
    }

    protected void initializeEmptyMoneyFile() {
        dao().createTables();
        dao().preload(t -> {});
    }

    protected Optional<?> get(Repository<?> repository, UUID uuid) {
        return dao().withNewConnection(connection -> {
            return repository.get(connection, uuid);
        });
    }
}
