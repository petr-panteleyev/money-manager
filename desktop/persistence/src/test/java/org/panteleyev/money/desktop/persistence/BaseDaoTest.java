/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.desktop.commons.DataCache;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class BaseDaoTest {
    private final static Logger LOGGER = Logger.getLogger(BaseDaoTest.class.getName());

    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    private static PostgreSQLContainer<?> container = null;

    protected static final DataCache cache = new DataCache();
    protected static final MoneyDAO dao = new MoneyDAO(cache);

    public static boolean setupAndSkip() {
        try {
            container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.12"))
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
            dao.initialize(dataSource);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void tearDown() throws Exception {
        if (container != null && container.isRunning()) {
            container.close();
            container.stop();
        }
    }

    protected void initializeEmptyMoneyFile() {
        dao.createTables();
        dao.preload(Runnable::run, t -> {});
    }

    protected Optional<?> get(Repository<?> repository, UUID uuid) {
        return dao.withNewConnection(connection -> {
            return repository.get(connection, uuid);
        });
    }
}
