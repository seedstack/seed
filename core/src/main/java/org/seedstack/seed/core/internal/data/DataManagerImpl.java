/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.seedstack.seed.DataExporter;
import org.seedstack.seed.DataImporter;
import org.seedstack.seed.DataManager;
import org.seedstack.seed.SeedException;

/**
 * Implementation of the {@link DataManager}.
 */
class DataManagerImpl implements DataManager {
    private static final String UTF_8 = "UTF-8";
    private static final String DATA_SET = "dataSet";
    private static final String IMPORTER_CLASS = "importerClass";
    private static final String GROUP = "group";
    private static final String NAME = "name";
    private static final String ITEMS = "items";
    private static final String CLASSES_MAP_KEY = "%s:%s";
    private final JsonFactory jsonFactory;
    private final ObjectMapper objectMapper;
    @Inject
    private Map<String, Map<String, DataExporterDefinition<Object>>> allDataExporters;
    @Inject
    private Map<String, Map<String, DataImporterDefinition<Object>>> allDataImporters;
    @Inject
    private Injector injector;

    DataManagerImpl() {
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper();
        this.jsonFactory.setCodec(this.objectMapper);
    }

    @Override
    public void exportData(OutputStream outputStream, String group) {
        Map<String, DataExporterDefinition<Object>> dataExporterDefinitions = allDataExporters.get(group);

        if (dataExporterDefinitions == null) {
            throw SeedException.createNew(DataErrorCode.NO_EXPORTER_FOUND).put(DATA_SET, group);
        }

        List<DataSetMarker<Object>> allIterators = new ArrayList<>();
        for (DataExporterDefinition<Object> dataExporterDefinition : dataExporterDefinitions.values()) {
            allIterators.add(new DataSetMarker<>(
                    dataExporterDefinition.getGroup(),
                    dataExporterDefinition.getName(),
                    injector.getInstance(dataExporterDefinition.getDataExporterClass()).exportData()
            ));
        }

        dumpAll(allIterators, outputStream);
    }

    @Override
    public void exportData(OutputStream outputStream, String group, String name) {
        Map<String, DataExporterDefinition<Object>> dataExporterDefinitionMap = allDataExporters.get(group);

        if (dataExporterDefinitionMap == null) {
            throw SeedException.createNew(DataErrorCode.NO_EXPORTER_FOUND).put(DATA_SET,
                    String.format(CLASSES_MAP_KEY, group, name));
        }

        DataExporterDefinition<Object> dataExporterDefinition = dataExporterDefinitionMap.get(name);

        if (dataExporterDefinition == null) {
            throw SeedException.createNew(DataErrorCode.NO_EXPORTER_FOUND).put(DATA_SET,
                    String.format(CLASSES_MAP_KEY, group, name));
        }

        dumpAll(Lists.newArrayList(new DataSetMarker(
                dataExporterDefinition.getGroup(),
                dataExporterDefinition.getName(),
                injector.getInstance(dataExporterDefinition.getDataExporterClass()).exportData()
        )), outputStream);
    }

    @Override
    public void exportData(OutputStream outputStream) {
        List<DataSetMarker<Object>> dataSets = new ArrayList<>();

        for (Map<String, DataExporterDefinition<Object>> dataExporterDefinitionMap : allDataExporters.values()) {
            for (DataExporterDefinition<Object> dataExporterDefinition : dataExporterDefinitionMap.values()) {
                DataExporter<Object> dataExporter = injector.getInstance(dataExporterDefinition.getDataExporterClass());
                dataSets.add(new DataSetMarker<>(dataExporterDefinition.getGroup(), dataExporterDefinition.getName(),
                        dataExporter.exportData()));
            }
        }

        dumpAll(dataSets, outputStream);
    }

    @Override
    public void importData(InputStream inputStream, String acceptGroup, String acceptName, boolean clear) {
        Set<DataImporter<Object>> usedDataImporters = new HashSet<>();

        try {
            ParsingState state = ParsingState.START;
            String group = null;
            String name = null;
            JsonParser jsonParser = this.jsonFactory.createParser(
                    new InputStreamReader(inputStream, Charset.forName(UTF_8)));
            JsonToken jsonToken = jsonParser.nextToken();

            while (jsonToken != null) {
                switch (state) {
                    case START:
                        if (jsonToken == JsonToken.START_ARRAY) {
                            state = ParsingState.DEFINITION_START;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "start array expected");
                        }

                        break;
                    case DEFINITION_START:
                        if (jsonToken == JsonToken.START_OBJECT) {
                            state = ParsingState.DEFINITION_GROUP;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "start object expected");
                        }

                        break;
                    case DEFINITION_GROUP:
                        if (jsonToken == JsonToken.FIELD_NAME && GROUP.equals(jsonParser.getCurrentName())) {
                            group = jsonParser.nextTextValue();
                            state = ParsingState.DEFINITION_NAME;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "group field expected");
                        }

                        break;
                    case DEFINITION_NAME:
                        if (jsonToken == JsonToken.FIELD_NAME && NAME.equals(jsonParser.getCurrentName())) {
                            name = jsonParser.nextTextValue();
                            state = ParsingState.DEFINITION_ITEMS;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "name field expected");
                        }

                        break;
                    case DEFINITION_ITEMS:
                        if (jsonToken == JsonToken.FIELD_NAME && ITEMS.equals(jsonParser.getCurrentName())) {
                            usedDataImporters.add(consumeItems(jsonParser, group, name, acceptGroup, acceptName));
                            state = ParsingState.DEFINITION_END;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "items field expected");
                        }

                        break;
                    case DEFINITION_END:
                        if (jsonToken == JsonToken.END_OBJECT) {
                            group = null;
                            name = null;
                            state = ParsingState.END;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "end object expected");
                        }

                        break;
                    case END:
                        if (jsonToken == JsonToken.START_OBJECT) {
                            state = ParsingState.DEFINITION_GROUP;
                        } else if (jsonToken == JsonToken.END_ARRAY) {
                            state = ParsingState.START;
                        } else {
                            throwParsingError(jsonParser.getCurrentLocation(), "start object or end array expected");
                        }

                        break;
                    default:
                        throwParsingError(jsonParser.getCurrentLocation(), "unexpected parser state");
                }

                jsonToken = jsonParser.nextToken();
            }
        } catch (IOException e1) {
            for (DataImporter<Object> usedDataImporter : usedDataImporters) {
                try {
                    usedDataImporter.rollback();
                } catch (RuntimeException e2) {
                    e2.initCause(e1);
                    throw SeedException.wrap(e2, DataErrorCode.FAILED_TO_ROLLBACK_IMPORT)
                            .put(IMPORTER_CLASS, usedDataImporter.getClass().getName());
                }
            }

            throw SeedException.wrap(e1, DataErrorCode.IMPORT_FAILED);
        }

        for (DataImporter<Object> usedDataImporter : usedDataImporters) {
            try {
                usedDataImporter.commit(clear);
            } catch (Exception e) {
                throw SeedException.wrap(e, DataErrorCode.FAILED_TO_COMMIT_IMPORT)
                        .put(IMPORTER_CLASS, usedDataImporter.getClass().getName());
            }
        }
    }

    private void throwParsingError(JsonLocation jsonLocation, String message) {
        throw SeedException.createNew(DataErrorCode.FAILED_TO_PARSE_DATA_STREAM)
                .put("parsingError", message)
                .put("line", jsonLocation.getLineNr())
                .put("col", jsonLocation.getColumnNr())
                .put("offset", jsonLocation.getCharOffset());
    }

    private DataImporter<Object> consumeItems(JsonParser jsonParser, String group, String name, String acceptGroup,
            String acceptName) throws IOException {

        Map<String, DataImporterDefinition<Object>> dataImporterDefinitionMap = allDataImporters.get(group);

        if (dataImporterDefinitionMap == null) {
            throw SeedException.createNew(DataErrorCode.NO_IMPORTER_FOUND)
                    .put(GROUP, group)
                    .put(NAME, name);
        }

        DataImporterDefinition<Object> currentImporterDefinition = dataImporterDefinitionMap.get(name);

        if (currentImporterDefinition == null) {
            throw SeedException.createNew(DataErrorCode.NO_IMPORTER_FOUND)
                    .put(GROUP, group)
                    .put(NAME, name);
        }

        if (!group.equals(currentImporterDefinition.getGroup())) {
            throw SeedException.createNew(DataErrorCode.UNEXPECTED_DATA_TYPE)
                    .put(DATA_SET, String.format(CLASSES_MAP_KEY, group, name))
                    .put(IMPORTER_CLASS, currentImporterDefinition.getDataImporterClass().getName());
        }

        if (!name.equals(currentImporterDefinition.getName())) {
            throw SeedException.createNew(DataErrorCode.UNEXPECTED_DATA_TYPE)
                    .put(DATA_SET, String.format(CLASSES_MAP_KEY, group, name))
                    .put(IMPORTER_CLASS, currentImporterDefinition.getDataImporterClass().getName());
        }

        DataImporter<Object> currentDataImporter = null;
        if ((acceptGroup == null || acceptGroup.equals(group)) && (acceptName == null || acceptName.equals(name))) {

            currentDataImporter = injector.getInstance(currentImporterDefinition.getDataImporterClass());

            // Check if items contains an array

            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Items should be an array");
            }

            jsonParser.nextToken();

            // If the array is not empty consume it
            if (jsonParser.getCurrentToken() != JsonToken.END_ARRAY) {
                Iterator<Object> objectIterator = jsonParser.readValuesAs(currentImporterDefinition.getImportedClass());

                while (objectIterator.hasNext()) {
                    currentDataImporter.importData(objectIterator.next());
                }

                // The array should end correctly
                if (jsonParser.getCurrentToken() != JsonToken.END_ARRAY) {
                    throw new IllegalArgumentException("end array expected");
                }
            }
        }

        // the data importer containing the data
        return currentDataImporter;
    }

    @Override
    public boolean isInitialized(String group, String name) {
        Map<String, DataImporterDefinition<Object>> dataImporterDefinitionMap = allDataImporters.get(group);

        if (dataImporterDefinitionMap == null) {
            throw SeedException.createNew(DataErrorCode.NO_IMPORTER_FOUND).put(GROUP, group).put(NAME, name);
        }

        DataImporterDefinition<Object> dataImporterDefinition = dataImporterDefinitionMap.get(name);

        if (dataImporterDefinition == null) {
            throw SeedException.createNew(DataErrorCode.NO_IMPORTER_FOUND).put(GROUP, group).put(NAME, name);
        }

        DataImporter<Object> dataImporter = injector.getInstance(dataImporterDefinition.getDataImporterClass());
        return dataImporter.isInitialized();
    }

    private void dumpAll(List<DataSetMarker<Object>> dataSetMarker, OutputStream outputStream) {
        try {
            JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(
                    new OutputStreamWriter(outputStream, Charset.forName(UTF_8)));
            ObjectWriter objectWriter = objectMapper.writer();

            jsonGenerator.writeStartArray();

            for (DataSetMarker<Object> objectDataSetMarker : dataSetMarker) {
                // start
                jsonGenerator.writeStartObject();

                // metadata
                jsonGenerator.writeStringField(GROUP, objectDataSetMarker.getGroup());
                jsonGenerator.writeStringField(NAME, objectDataSetMarker.getName());

                // items
                jsonGenerator.writeArrayFieldStart(ITEMS);
                while (objectDataSetMarker.getItems().hasNext()) {
                    objectWriter.writeValue(jsonGenerator, objectDataSetMarker.getItems().next());
                }
                jsonGenerator.writeEndArray();

                // end
                jsonGenerator.writeEndObject();
            }

            jsonGenerator.writeEndArray();

            jsonGenerator.flush();
        } catch (Exception e) {
            throw SeedException.wrap(e, DataErrorCode.EXPORT_FAILED);
        }
    }

    private enum ParsingState {
        START,
        DEFINITION_START,
        DEFINITION_GROUP,
        DEFINITION_NAME,
        DEFINITION_ITEMS,
        DEFINITION_END,
        END
    }
}
