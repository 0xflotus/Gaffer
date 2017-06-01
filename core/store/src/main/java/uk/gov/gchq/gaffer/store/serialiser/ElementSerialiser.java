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
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.ToBytesSerialiser;
import uk.gov.gchq.gaffer.store.schema.Schema;

public class ElementSerialiser extends PropertiesSerialiser implements ToBytesSerialiser<Element> {
    private final EntitySerialiser entitySerialiser;
    private final EdgeSerialiser edgeSerialiser;

    public ElementSerialiser(final Schema schema) {
        super(schema);
        entitySerialiser = new EntitySerialiser(schema);
        edgeSerialiser = new EdgeSerialiser(schema);
    }

    @Override
    public boolean canHandle(final Class clazz) {
        return Element.class.isAssignableFrom(clazz);
    }

    @Override
    public byte[] serialise(final Element element) throws SerialisationException {
        if (element instanceof Entity) {
            return entitySerialiser.serialise(((Entity) element));
        }

        return edgeSerialiser.serialise(((Edge) element));
    }

    @Override
    public Element deserialise(final byte[] bytes) throws SerialisationException {
        final byte[] groupBytes = getFieldBytes(bytes, 0);
        final String group = StringUtil.toString(groupBytes);

        if (null != schema.getEntity(group)) {
            return entitySerialiser.deserialise(bytes);
        }

        return edgeSerialiser.deserialise(bytes);
    }

    @Override
    public Element deserialiseEmpty() throws SerialisationException {
        return null;
    }
}
