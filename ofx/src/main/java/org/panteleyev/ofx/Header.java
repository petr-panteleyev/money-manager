package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public record Header(String ofxHeader,
                     String version,
                     SecurityEnum security,
                     String oldFileUid,
                     String newFileUid)
{

    public Header(String ofxHeader, String version, String security, String oldFileUid, String newFileUid) {
        this(ofxHeader == null ? "NONE" : ofxHeader,
            version == null ? "NONE" : version,
            security == null ? SecurityEnum.NONE : SecurityEnum.valueOf(security),
            oldFileUid == null ? "NONE" : oldFileUid,
            newFileUid == null ? "NONE" : newFileUid);
    }
}
