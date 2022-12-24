/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record MoneyDocument(
        UUID uuid,
        UUID ownerUuid,
        UUID contactUuid,
        DocumentType documentType,
        String fileName,
        LocalDate date,
        int size,
        boolean compressed,
        String mimeType,
        String description,
        long created,
        long modified
) implements MoneyRecord {

    public MoneyDocument {
        Objects.requireNonNull(contactUuid, "Document contact UUID must not be null");

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (ownerUuid == null) {
            ownerUuid = contactUuid;
        }
        if (documentType == null) {
            documentType = DocumentType.OTHER;
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalStateException("File name cannot be null or blank");
        }

        mimeType = MoneyRecord.normalize(mimeType);
        description = MoneyRecord.normalize(description);

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public static final class Builder {
        private UUID uuid;
        private UUID ownerUuid;
        private UUID contactUuid;
        private DocumentType documentType = DocumentType.OTHER;
        private String fileName;
        private LocalDate date = LocalDate.now();
        private int size;
        private boolean compressed;
        private String mimeType;
        private String description;
        private long created;
        private long modified;

        public Builder() {
        }

        public Builder(MoneyDocument moneyDocument) {
            if (moneyDocument == null) {
                return;
            }

            uuid = moneyDocument.uuid();
            ownerUuid = moneyDocument.ownerUuid();
            contactUuid = moneyDocument.contactUuid();
            documentType = moneyDocument.documentType();
            fileName = moneyDocument.fileName();
            date = moneyDocument.date();
            size = moneyDocument.size();
            compressed = moneyDocument.compressed();
            mimeType = moneyDocument.mimeType();
            description = moneyDocument.description();
            created = moneyDocument.created();
            modified = moneyDocument.modified();
        }

        public MoneyDocument build() {
            return new MoneyDocument(uuid, ownerUuid, contactUuid, documentType, fileName, date,
                    size, compressed, mimeType, description, created, modified
            );
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder ownerUuid(UUID ownerUuid) {
            this.ownerUuid = ownerUuid;
            return this;
        }

        public Builder contactUuid(UUID contactUuid) {
            this.contactUuid = contactUuid;
            return this;
        }

        public Builder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
