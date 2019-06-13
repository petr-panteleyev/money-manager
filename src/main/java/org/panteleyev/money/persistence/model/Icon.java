/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.persistence.model;

import javafx.scene.image.Image;
import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.PrimaryKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Table("icon")
public class Icon implements MoneyRecord {
    public static final int ICON_SIZE = 16;
    public static final int ICON_BYTE_LENGTH = 8192;

    @PrimaryKey
    @Column("uuid")
    private final UUID uuid;
    @Column("name")
    private final String name;
    @Column(value = "bytes", length = ICON_BYTE_LENGTH)
    private final byte[] bytes;
    @Column("created")
    private final long created;
    @Column("modified")
    private final long modified;

    private final Image image;

    @RecordBuilder
    public Icon(@Column("uuid") UUID uuid,
                @Column("name") String name,
                @Column("bytes") byte[] bytes,
                @Column("created") long created,
                @Column("modified") long modified)
    {
        this.uuid = uuid;
        this.name = name;
        this.bytes = bytes;
        this.created = created;
        this.modified = modified;

        try (var inputStream = new ByteArrayInputStream(bytes)) {
            this.image = new Image(inputStream, ICON_SIZE, ICON_SIZE, true, true);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Icon(UUID uuid, String name, byte[] bytes, Image image, long created, long modified) {
        this.uuid = uuid;
        this.name = name;
        this.bytes = bytes;
        this.created = created;
        this.modified = modified;
        this.image = image;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Icon)) {
            return false;
        }

        var that = (Icon) other;
        return Objects.equals(uuid, that.uuid)
            && Objects.equals(name, that.name)
            && Arrays.equals(bytes, that.bytes)
            && created == that.created
            && modified == that.modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, bytes, created, modified);
    }

    @Override
    public String toString() {
        return "Icon ["
            + "uuid:" + uuid
            + " name:" + name
            + "]";
    }
}
