/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getLocalDate;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class DocumentRepository implements MoneyRepository<MoneyDocument> {
    private static final RowMapper<MoneyDocument> ROW_MAPPER = (rs, i) -> new MoneyDocument(
            getUuid(rs, "uuid"),
            getUuid(rs, "owner_uuid"),
            getUuid(rs, "contact_uuid"),
            getEnum(rs, "document_type", DocumentType.class),
            rs.getString("file_name"),
            getLocalDate(rs, "file_date"),
            rs.getInt("file_size"),
            rs.getString("mime_type"),
            rs.getString("description"),
            rs.getLong("created"),
            rs.getLong("modified")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DocumentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MoneyDocument> getAll() {
        return jdbcTemplate.query("SELECT * FROM document", ROW_MAPPER);
    }

    @Override
    public Stream<MoneyDocument> getStream() {
        return jdbcTemplate.queryForStream("SELECT * FROM document", Map.of(), ROW_MAPPER);
    }

    @Override
    public Optional<MoneyDocument> get(UUID uuid) {
        var queryResult = jdbcTemplate.query(
                "SELECT * FROM document WHERE uuid = :id",
                Map.of("id", uuid),
                ROW_MAPPER);
        return queryResult.size() == 0 ?
                Optional.empty() :
                Optional.of(queryResult.get(0));
    }

    @Override
    public int insertOrUpdate(MoneyDocument document) {
        return jdbcTemplate.update("""
                        INSERT INTO document (
                            uuid, owner_uuid, contact_uuid, document_type, file_name, file_date, file_size,
                            mime_type, description, created, modified
                        ) VALUES (
                            :uuid, :ownerUuid, :contactUuid, :documentType, :fileName, :fileDate, :fileSize,
                            :mimeType, :description, :created, :modified
                        ) ON CONFLICT (uuid) DO UPDATE SET
                            owner_uuid = :ownerUuid,
                            contact_uuid = :contactUuid,
                            document_type = :documentType,
                            file_name = :fileName,
                            file_date = :fileDate,
                            file_size = :fileSize,
                            mime_type = :mimeType,
                            description = :description,
                            modified = :modified
                        """,
                Map.ofEntries(
                        Map.entry("uuid", document.uuid()),
                        Map.entry("ownerUuid", document.ownerUuid()),
                        Map.entry("contactUuid", document.contactUuid()),
                        Map.entry("documentType", document.documentType().name()),
                        Map.entry("fileName", document.fileName()),
                        Map.entry("fileDate", document.date().toEpochDay()),
                        Map.entry("fileSize", document.size()),
                        Map.entry("mimeType", document.mimeType()),
                        Map.entry("description", document.description()),
                        Map.entry("created", document.created()),
                        Map.entry("modified", document.modified())
                )
        );
    }
}
