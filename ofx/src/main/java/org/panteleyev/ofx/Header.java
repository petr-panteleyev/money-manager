/*
 Copyright © 2020 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

public record Header(String ofxHeader,
                     String version,
                     SecurityEnum security,
                     String oldFileUid,
                     String newFileUid) {

    public Header(String ofxHeader, String version, String security, String oldFileUid, String newFileUid) {
        this(ofxHeader == null ? "NONE" : ofxHeader,
                version == null ? "NONE" : version,
                security == null ? SecurityEnum.NONE : SecurityEnum.valueOf(security),
                oldFileUid == null ? "NONE" : oldFileUid,
                newFileUid == null ? "NONE" : newFileUid);
    }
}
