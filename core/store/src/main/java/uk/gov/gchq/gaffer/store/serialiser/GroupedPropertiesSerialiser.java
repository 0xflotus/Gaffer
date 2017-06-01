/*
 * Copyright 2016-2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.store.serialiser;

import uk.gov.gchq.gaffer.commonutil.StringUtil;
import uk.gov.gchq.gaffer.data.element.GroupedProperties;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.ToBytesSerialiser;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaElementDefinition;

public class GroupedPropertiesSerialiser extends PropertiesSerialiser implements ToBytesSerialiser<GroupedProperties> {

    public GroupedPropertiesSerialiser(final Schema schema) {
        super(schema);
    }

    public boolean canHandle(final Class clazz) {
        return GroupedProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public byte[] serialise(final GroupedProperties properties) throws SerialisationException {
        if (null == properties) {
            return new byte[0];
        }

        if (null == properties.getGroup() || properties.getGroup().isEmpty()) {
            throw new IllegalArgumentException("Group is required for serialising " + GroupedProperties.class.getSimpleName());
        }

        return serialiseProperties(properties, properties.getGroup());
    }

    @Override
    public GroupedProperties deserialise(final byte[] bytes) throws SerialisationException {
        int lastDelimiter = 0;

        final byte[] groupBytes = getFieldBytes(bytes, lastDelimiter);
        final String group = StringUtil.toString(groupBytes);
        lastDelimiter = getLastDelimiter(bytes, groupBytes, lastDelimiter);

        if (group.isEmpty()) {
            throw new IllegalArgumentException("Group is required for deserialising " + GroupedProperties.class.getSimpleName());
        }

        final SchemaElementDefinition elementDefinition = schema.getElement(group);
        if (null == elementDefinition) {
            throw new SerialisationException("No SchemaElementDefinition found for group " + group + ", is this group in your schema or do your table iterators need updating?");
        }

        final GroupedProperties properties = new GroupedProperties(group);
        deserialiseProperties(bytes, properties, elementDefinition, lastDelimiter);
        return properties;
    }

    @Override
    public GroupedProperties deserialiseEmpty() throws SerialisationException {
        return null;
    }
}
