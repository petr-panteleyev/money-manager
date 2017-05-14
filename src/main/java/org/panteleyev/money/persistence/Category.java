/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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
package org.panteleyev.money.persistence;

import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.util.Objects;
import java.util.Optional;

@Table("category")
public class Category implements Record, Named {
    public static class Builder {
        private int id;
        private String  name;
        private CategoryType type;
        private String  comment;
        private boolean expanded;

        public Builder() {
            expanded = false;
        }

        public Builder(Category c) {
            this();

            if (c != null) {
                this.id = c.getId();
                this.name = c.getName();
                this.type = c.getCatType();
                this.comment = c.getComment();
                this.expanded = c.isExpanded();
            }
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public int id() {
            return id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder expanded(boolean expanded) {
            this.expanded = expanded;
            return this;
        }

        public Category build() {
            if (id == 0) {
                throw new IllegalStateException("Category.id == 0");
            }

            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            return new Category(id, name, comment, type, expanded);
        }
    }

    private final int id;
    private final String  name;
    private final String  comment;
    private final CategoryType catType;
    private final boolean expanded;

    @RecordBuilder
    public Category(
            @Field(Field.ID) int id,
            @Field("name") String name,
            @Field("comment") String comment,
            @Field("cat_type") CategoryType catType,
            @Field("expanded") boolean expanded
    ) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.catType = catType;
        this.expanded = expanded;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public int getId() {
        return id;
    }

    @Field("name")
    @Override
    public String getName() {
        return name;
    }

    @Field("comment")
    public String getComment() {
        return comment;
    }

    @Field(value = "cat_type", nullable=false)
    public CategoryType getCatType() {
        return catType;
    }

    @Field("expanded")
    public boolean isExpanded() {
        return expanded;
    }

    public Category expand(boolean exp) {
        return new Category(id, name, comment, catType, exp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Category) {
            Category that = (Category)obj;
            return this.id == that.id
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.catType, that.catType)
                    && this.expanded == that.expanded;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, comment, catType, expanded);
    }
}
