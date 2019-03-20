/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package custom;

import java.io.File;
import java.util.Map;
import javax.inject.Provider;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.ClassConfiguration;
import org.seedstack.seed.Provide;

@Provide(override = true)
public class CustomApplicationProvider implements Provider<Application> {
    @Override
    public Application get() {
        return new Application() {
            @Override
            public String getName() {
                return "custom";
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public File getStorageLocation(String context) {
                return null;
            }

            @Override
            public boolean isStorageEnabled() {
                return false;
            }

            @Override
            public Coffig getConfiguration() {
                return null;
            }

            @Override
            public <T> ClassConfiguration<T> getConfiguration(Class<T> someClass) {
                return null;
            }

            @Override
            public String substituteWithConfiguration(String value) {
                return null;
            }

            @Override
            public Map<String, String> getKernelParameters() {
                return null;
            }

            @Override
            public String[] getArguments() {
                return new String[0];
            }
        };
    }
}
