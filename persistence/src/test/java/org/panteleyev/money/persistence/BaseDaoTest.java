package org.panteleyev.money.persistence;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.mysql.cj.jdbc.MysqlDataSource;
import org.panteleyev.money.test.BaseTest;
import org.testng.SkipException;
import javax.sql.DataSource;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class BaseDaoTest extends BaseTest {
    private static final String TEST_DB_NAME = "TestDB";

    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    private DataSource dataSource;
    private String databaseName;

    public void setupAndSkip() throws Exception {
        databaseName = System.getProperty("mysql.database", TEST_DB_NAME);
        var host = System.getProperty("mysql.host", "localhost");
        var user = System.getProperty("mysql.user", null);
        var password = System.getProperty("mysql.password", null);

        if (user == null || password == null) {
            throw new SkipException("Test config is not set");
        }

        dataSource = new MySQLBuilder()
            .host(host)
            .user(user)
            .password(password)
            .build();

        try (var conn = dataSource.getConnection(); var st = conn.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS " + databaseName);
            st.execute("CREATE DATABASE " + databaseName);
            ((MysqlDataSource) dataSource).setDatabaseName(databaseName);
        }

        getDao().initialize(dataSource);
    }

    public void cleanup() throws Exception {
        try (var conn = dataSource.getConnection(); var st = conn.createStatement()) {
            st.execute("DROP DATABASE " + databaseName);
        }
    }

    protected void initializeEmptyMoneyFile() {
        getDao().createTables();
        getDao().preload(t -> { });
    }
}
