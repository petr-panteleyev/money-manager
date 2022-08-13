/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static java.util.Map.entry;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class ContactRepository implements MoneyRepository<Contact> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Contact> rowMapper = (rs, i) -> new Contact(
        getUuid(rs, "uuid"),
        rs.getString("name"),
        getEnum(rs, "type", ContactType.class),
        rs.getString("phone"),
        rs.getString("mobile"),
        rs.getString("email"),
        rs.getString("web"),
        rs.getString("comment"),
        rs.getString("street"),
        rs.getString("city"),
        rs.getString("country"),
        rs.getString("zip"),
        getUuid(rs, "icon_uuid"),
        rs.getLong("created"),
        rs.getLong("modified")
    );

    private static Map<String, Object> toMap(Contact contact) {
        var map = new HashMap<String, Object>(Map.ofEntries(
            entry("uuid", contact.uuid()),
            entry("name", contact.name()),
            entry("type", contact.type().name()),
            entry("phone", contact.phone()),
            entry("mobile", contact.mobile()),
            entry("email", contact.email()),
            entry("web", contact.web()),
            entry("comment", contact.comment()),
            entry("street", contact.street()),
            entry("city", contact.city()),
            entry("country", contact.country()),
            entry("zip", contact.zip()),
            entry("created", contact.created()),
            entry("modified", contact.modified())
        ));
        map.put("iconUuid", contact.iconUuid());
        return map;
    }

    public ContactRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Contact> getAll() {
        return jdbcTemplate.query("SELECT * FROM contact", rowMapper);
    }

    @Override
    public int insert(Contact contact) {
        return jdbcTemplate.update("""
                INSERT INTO contact (uuid, name, type, phone, mobile, email, web, comment, street, city,
                    country, zip, icon_uuid, created, modified
                ) VALUES (
                    :uuid, :name, :type, :phone, :mobile, :email, :web, :comment, :street, :city,
                    :country, :zip, :iconUuid, :created, :modified
                )
                """,
            toMap(contact)
        );
    }

    @Override
    public int update(Contact contact) {
        return jdbcTemplate.update("""
                UPDATE contact SET
                    name = :name, type = :type, phone = :phone, mobile = :mobile, email = :email, web = :web,
                    comment = :comment, street = :street, city = :city, country = :country, zip = :zip,
                    icon_uuid = :iconUuid, modified = :modified
                WHERE uuid = :uuid
                """,
            toMap(contact)
        );
    }

    @Override
    public Optional<Contact> get(UUID uuid) {
        var queryResult = jdbcTemplate.query(
            "SELECT * FROM contact WHERE uuid = :id",
            Map.of("id", uuid.toString()),
            rowMapper);
        return queryResult.size() == 0 ?
            Optional.empty() :
            Optional.of(queryResult.get(0));

    }
}
