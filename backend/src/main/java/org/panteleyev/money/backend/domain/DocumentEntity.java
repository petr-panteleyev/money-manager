/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "Document")
@Table(name = "document")
public class DocumentEntity {
    private UUID uuid;
    private UUID ownerUuid;
    private ContactEntity contact;
    private String documentType;
    private String fileName;
    private LocalDate fileDate;
    private int fileSize;
    private String mimeType;
    private String description;
    private byte[] content;
    private long created;
    private long modified;

    public DocumentEntity() {
    }

    @Id
    public UUID getUuid() {
        return uuid;
    }

    public DocumentEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public DocumentEntity setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_uuid", nullable = false)
    public ContactEntity getContact() {
        return contact;
    }

    public DocumentEntity setContact(ContactEntity contact) {
        this.contact = contact;
        return this;
    }

    @Column(nullable = false)
    public String getDocumentType() {
        return documentType;
    }

    public DocumentEntity setDocumentType(String documentType) {
        this.documentType = documentType;
        return this;
    }

    @Column(nullable = false)
    public String getFileName() {
        return fileName;
    }

    public DocumentEntity setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Column(nullable = false)
    public LocalDate getFileDate() {
        return fileDate;
    }

    public DocumentEntity setFileDate(LocalDate fileDate) {
        this.fileDate = fileDate;
        return this;
    }

    @Column(nullable = false)
    public int getFileSize() {
        return fileSize;
    }

    public DocumentEntity setFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    @Column(nullable = false)
    public String getMimeType() {
        return mimeType;
    }

    public DocumentEntity setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    @Column(nullable = false)
    public String getDescription() {
        return description;
    }

    public DocumentEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    @Basic(fetch = FetchType.LAZY)
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Column(nullable = false)
    public long getCreated() {
        return created;
    }

    public DocumentEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    @Column(nullable = false)
    public long getModified() {
        return modified;
    }

    public DocumentEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }
}
