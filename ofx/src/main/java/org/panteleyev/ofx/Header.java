package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public class Header {
    private final String ofxHeader;
    private final String version;
    private final SecurityEnum security;
    private final String oldFileUid;
    private final String newFileUid;

    Header(String ofxHeader, String version, String security, String oldFileUid, String newFileUid) {
        this.ofxHeader = ofxHeader == null ? "NONE" : ofxHeader;
        this.version = version == null ? "NONE" : version;
        this.security = security == null ? SecurityEnum.NONE : SecurityEnum.valueOf(security);
        this.oldFileUid = oldFileUid == null ? "NONE" : oldFileUid;
        this.newFileUid = newFileUid == null ? "NONE" : newFileUid;
    }

    public String getOfxHeader() {
        return ofxHeader;
    }

    public String getVersion() {
        return version;
    }

    public SecurityEnum getSecurity() {
        return security;
    }

    public String getOldFileUid() {
        return oldFileUid;
    }

    public String getNewFileUid() {
        return newFileUid;
    }
}
