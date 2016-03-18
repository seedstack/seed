/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.seedstack.seed.spi.command.CommandDefinition;
import org.seedstack.seed.spi.command.Option;
import org.seedstack.seed.spi.command.PrettyCommand;

/**
 * This command serialize the input object to json.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "", name = "json", description = "Serialize the input object to json")
public class JsonCommand implements PrettyCommand<String> {
    private static final ObjectMapper OBJECT_MAPPER;
    private static final DefaultPrettyPrinter DEFAULT_PRETTY_PRINTER;

    @Option(name = "p", longName = "pretty", description = "Output pretty JSON directly")
    private boolean pretty;

    static {
        DEFAULT_PRETTY_PRINTER = new DefaultPrettyPrinter();
        DEFAULT_PRETTY_PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String execute(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }

        if (pretty) {
            return OBJECT_MAPPER.writer(DEFAULT_PRETTY_PRINTER).writeValueAsString(object);
        } else {
            return OBJECT_MAPPER.writeValueAsString(object);
        }
    }

    @Override
    public String prettify(String object) throws Exception {
        return OBJECT_MAPPER.writer(DEFAULT_PRETTY_PRINTER).writeValueAsString(OBJECT_MAPPER.readValue(object, Object.class));
    }
}
