/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

class ApplicationInfo {
    private String appId;
    private String appName;
    private String appVersion;

    String getId() {
        return appId;
    }

    String getName() {
        return appName;
    }

    String getVersion() {
        return appVersion;
    }

    void setAppId(String appId) {
        this.appId = appId;
    }

    void setAppName(String appName) {
        this.appName = appName;
    }

    void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return String.format("%s v%s", appName, appVersion);
    }
}
