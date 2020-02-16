package org.panteleyev.money.persistence;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.mysql.cj.jdbc.MysqlDataSource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.TimeZone;

public class MySQLBuilder {
    private int port = 3306;
    private String host = "localhost";
    private String dbName = "";
    private String user = "";
    private String password = "";

    public DataSource build() {
        try {
            var ds = new MysqlDataSource();
            ds.setCharacterEncoding("utf8");
            ds.setUseSSL(false);
            ds.setServerTimezone(TimeZone.getDefault().getID());
            ds.setPort(port);
            ds.setServerName(host);
            ds.setUser(user);
            ds.setPassword(password);
            ds.setDatabaseName(dbName);
            return ds;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public MySQLBuilder host(String host) {
        this.host = host;
        return this;
    }

    public MySQLBuilder port(int port) {
        this.port = port;
        return this;
    }

    public MySQLBuilder name(String name) {
        this.dbName = name;
        return this;
    }

    public MySQLBuilder user(String user) {
        this.user = user;
        return this;
    }

    public MySQLBuilder password(String password) {
        this.password = password;
        return this;
    }

    public String getConnectionString() {
        return "mysql://" + host + ":" + port + "/" + dbName;
    }
}
