/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import javafx.embed.swing.JFXPanel;
import org.h2.jdbcx.JdbcDataSource;
import org.panteleyev.money.test.BaseTest;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class BaseDaoTest extends BaseTest {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    public void setupAndSkip() throws Exception {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        try (var conn = dataSource.getConnection()) {
            new LiquibaseUtil(conn).update();
        }
        getDao().initialize(dataSource);
        new JFXPanel();
    }

    protected void initializeEmptyMoneyFile() {
        getDao().createTables();
        getDao().preload(t -> { });
    }
}
