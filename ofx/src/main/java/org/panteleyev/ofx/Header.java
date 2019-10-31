/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.ofx;

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
