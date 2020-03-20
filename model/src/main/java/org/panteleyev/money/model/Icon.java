package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.image.Image;
import org.panteleyev.mysqlapi.annotations.Column;
import org.panteleyev.mysqlapi.annotations.PrimaryKey;
import org.panteleyev.mysqlapi.annotations.RecordBuilder;
import org.panteleyev.mysqlapi.annotations.Table;
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
    public UUID uuid() {
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
    public long created() {
        return created;
    }

    @Override
    public long modified() {
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
