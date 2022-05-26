/*
 Copyright (C) 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.persistence;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSetStatus;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class LiquibaseUtil {
    public enum SchemaStatus {
        UP_TO_DATE,
        UPDATE_REQUIRED,
        INCOMPATIBLE
    }

    private static final String CHANGELOG_XML = "/org/panteleyev/money/liquibase/masterChangelog.xml";

    private final Liquibase liquibase;

    public LiquibaseUtil(Connection connection) {
        try {
            liquibase = new Liquibase(CHANGELOG_XML,
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(connection));
        } catch (LiquibaseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update() {
        try {
            liquibase.update(new Contexts());
        } catch (LiquibaseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void dropAndUpdate() {
        try {
            liquibase.dropAll();
            update();
        } catch (LiquibaseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public SchemaStatus checkSchemaUpdateStatus() {
        try {
            var report = liquibase.getChangeSetStatuses(new Contexts(), new LabelExpression(), true);
            return report.stream().anyMatch(ChangeSetStatus::getWillRun) ?
                    SchemaStatus.UPDATE_REQUIRED : SchemaStatus.UP_TO_DATE;
        } catch (LiquibaseException ex) {
            return SchemaStatus.INCOMPATIBLE;
        }
    }
}
