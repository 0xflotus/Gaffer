/*
 * Copyright 2016 Crown Copyright
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
package gaffer.accumulostore.key.impl;

import gaffer.accumulostore.MockAccumuloStore;
import gaffer.accumulostore.MockAccumuloStoreForTest;
import gaffer.accumulostore.key.AccumuloElementConverter;
import gaffer.accumulostore.key.RangeFactory;
import gaffer.accumulostore.key.core.impl.byteEntity.*;
import gaffer.accumulostore.key.core.impl.classic.ClassicAccumuloElementConverter;
import gaffer.accumulostore.key.core.impl.classic.ClassicKeyPackage;
import gaffer.accumulostore.key.exception.AccumuloElementConversionException;
import gaffer.accumulostore.key.exception.IteratorSettingException;
import gaffer.accumulostore.key.exception.RangeFactoryException;
import gaffer.accumulostore.operation.impl.SummariseGroupOverRanges;
import gaffer.accumulostore.utils.*;
import gaffer.commonutil.TestGroups;
import gaffer.data.element.*;
import gaffer.data.element.Properties;
import gaffer.operation.data.ElementSeed;
import gaffer.operation.data.EntitySeed;
import gaffer.store.StoreException;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.*;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class RowIdAggregatorTest {

    private MockAccumuloStore byteEntityStore;
    private MockAccumuloStore gaffer1KeyStore;
    private AccumuloElementConverter byteEntityElementConverter;
    private AccumuloElementConverter gaffer1ElementConverter;

    @Before
    public void setup() throws StoreException, AccumuloException, AccumuloSecurityException, IOException {
        byteEntityStore = new MockAccumuloStoreForTest(ByteEntityKeyPackage.class);
        gaffer1KeyStore = new MockAccumuloStoreForTest(ClassicKeyPackage.class);

        byteEntityStore.getProperties().setTable("Test");
        gaffer1KeyStore.getProperties().setTable("Test2");

        gaffer1ElementConverter = new ClassicAccumuloElementConverter(gaffer1KeyStore.getSchema());
        byteEntityElementConverter = new ByteEntityAccumuloElementConverter(byteEntityStore.getSchema());
    }


    @Test
    public void testMultiplePropertySetsAggregateAcrossRowIDInByteEntityStore() throws StoreException, AccumuloElementConversionException, RangeFactoryException {
        testAggregatingMultiplePropertySetsAcrossRowIDRange(byteEntityStore, byteEntityElementConverter);
    }

    @Test
    public void testMultiplePropertySetsAggregateAcrossRowIDInGafferOneStore() throws StoreException, AccumuloElementConversionException, RangeFactoryException {
        testAggregatingMultiplePropertySetsAcrossRowIDRange(gaffer1KeyStore, gaffer1ElementConverter);
    }

    private void testAggregatingMultiplePropertySetsAcrossRowIDRange(final MockAccumuloStore store, final AccumuloElementConverter elementConverter) throws StoreException, AccumuloElementConversionException, RangeFactoryException {
        String visibilityString = "public";
        try {
            // Create table
            // (this method creates the table, removes the versioning iterator, and adds the SetOfStatisticsCombiner iterator).
            TableUtils.createTable(store);

            final Properties properties1 = new Properties();
            properties1.put(AccumuloPropertyNames.COUNT, 1);

            final Properties properties2 = new Properties();
            properties2.put(AccumuloPropertyNames.COUNT, 1);

            final Properties properties3 = new Properties();
            properties3.put(AccumuloPropertyNames.COUNT, 2);

            // Create edge
            final Edge edge = new Edge(TestGroups.EDGE);
            edge.setSource("2");
            edge.setDestination("1");
            edge.setDirected(true);
            edge.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 1);
            edge.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge.putProperty(AccumuloPropertyNames.PROP_4, 0);

            final Edge edge2 = new Edge(TestGroups.EDGE);
            edge2.setSource("B");
            edge2.setDestination("Z");
            edge2.setDirected(true);
            edge2.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 1);
            edge2.putProperty(AccumuloPropertyNames.PROP_1, 1);
            edge2.putProperty(AccumuloPropertyNames.PROP_2, 1);
            edge2.putProperty(AccumuloPropertyNames.PROP_3, 1);
            edge2.putProperty(AccumuloPropertyNames.PROP_4, 1);

            final Edge edge3 = new Edge(TestGroups.EDGE);
            edge3.setSource("3");
            edge3.setDestination("8");
            edge3.setDirected(true);
            edge3.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 1);
            edge3.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge3.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge3.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge3.putProperty(AccumuloPropertyNames.PROP_4, 0);

            final Edge edge6 = new Edge("BasicEdge2");
            edge6.setSource("1");
            edge6.setDestination("5");
            edge6.setDirected(true);
            edge6.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 2);
            edge6.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge6.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge6.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge6.putProperty(AccumuloPropertyNames.PROP_4, 0);


            final Edge edge7 = new Edge("BasicEdge2");
            edge7.setSource("2");
            edge7.setDestination("6");
            edge7.setDirected(true);
            edge7.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 1);
            edge7.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge7.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge7.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge7.putProperty(AccumuloPropertyNames.PROP_4, 0);

            final Edge edge8 = new Edge("BasicEdge2");
            edge8.setSource("4");
            edge8.setDestination("8");
            edge8.setDirected(true);
            edge8.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 2);
            edge8.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge8.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge8.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge8.putProperty(AccumuloPropertyNames.PROP_4, 0);

            final Edge edge9 = new Edge("BasicEdge2");
            edge9.setSource("5");
            edge9.setDestination("9");
            edge9.setDirected(true);
            edge9.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 2);
            edge9.putProperty(AccumuloPropertyNames.PROP_1, 0);
            edge9.putProperty(AccumuloPropertyNames.PROP_2, 0);
            edge9.putProperty(AccumuloPropertyNames.PROP_3, 0);
            edge9.putProperty(AccumuloPropertyNames.PROP_4, 0);

            // Accumulo key
            final Key key = elementConverter.getKeysFromEdge(edge).getFirst();
            final Key key2 = elementConverter.getKeysFromEdge(edge2).getFirst();
            final Key key3 = elementConverter.getKeysFromEdge(edge3).getFirst();
            final Key key4 = elementConverter.getKeysFromEdge(edge6).getFirst();
            final Key key5 = elementConverter.getKeysFromEdge(edge7).getFirst();
            final Key key6 = elementConverter.getKeysFromEdge(edge8).getFirst();
            final Key key7 = elementConverter.getKeysFromEdge(edge9).getFirst();

            // Accumulo values
            final Value value1 = elementConverter.getValueFromProperties(properties1, TestGroups.EDGE);
            final Value value2 = elementConverter.getValueFromProperties(properties2, TestGroups.EDGE);
            final Value value3 = elementConverter.getValueFromProperties(properties3, TestGroups.EDGE);
            final Value value4 = elementConverter.getValueFromProperties(properties1, TestGroups.EDGE_2);
            final Value value5 = elementConverter.getValueFromProperties(properties2, TestGroups.EDGE_2);

            //Create mutation
            final Mutation m1 = new Mutation(key.getRow());
            m1.put(key.getColumnFamily(), key.getColumnQualifier(), new ColumnVisibility(key.getColumnVisibility()), key.getTimestamp(), value1);
            final Mutation m2 = new Mutation(key.getRow());
            m2.put(key.getColumnFamily(), key.getColumnQualifier(), new ColumnVisibility(key.getColumnVisibility()), key.getTimestamp(), value2);
            final Mutation m3 = new Mutation(key.getRow());
            m3.put(key.getColumnFamily(), key.getColumnQualifier(), new ColumnVisibility(key.getColumnVisibility()), key.getTimestamp(), value3);
            final Mutation m4 = new Mutation(key2.getRow());
            m4.put(key2.getColumnFamily(), key2.getColumnQualifier(), new ColumnVisibility(key2.getColumnVisibility()), key2.getTimestamp(), value1);
            final Mutation m5 = new Mutation(key.getRow());
            m5.put(key3.getColumnFamily(), key3.getColumnQualifier(), new ColumnVisibility(key3.getColumnVisibility()), key3.getTimestamp(), value1);
            final Mutation m6 = new Mutation(key4.getRow());
            m6.put(key4.getColumnFamily(), key4.getColumnQualifier(), new ColumnVisibility(key4.getColumnVisibility()), key4.getTimestamp(), value4);
            final Mutation m7 = new Mutation(key5.getRow());
            m7.put(key5.getColumnFamily(), key5.getColumnQualifier(), new ColumnVisibility(key5.getColumnVisibility()), key5.getTimestamp(), value5);
            final Mutation m8 = new Mutation(key6.getRow());
            m8.put(key6.getColumnFamily(), key6.getColumnQualifier(), new ColumnVisibility(key6.getColumnVisibility()), key6.getTimestamp(), value5);
            final Mutation m9 = new Mutation(key7.getRow());
            m9.put(key7.getColumnFamily(), key7.getColumnQualifier(), new ColumnVisibility(key7.getColumnVisibility()), key7.getTimestamp(), value5);

            // Write mutation
            final BatchWriterConfig writerConfig = new BatchWriterConfig();
            writerConfig.setMaxMemory(1000000L);
            writerConfig.setMaxLatency(1000L, TimeUnit.MILLISECONDS);
            writerConfig.setMaxWriteThreads(1);
            final BatchWriter writer = store.getMockConnector().createBatchWriter(store.getProperties().getTable(), writerConfig);
            writer.addMutation(m1);
            writer.addMutation(m2);
            writer.addMutation(m3);
            writer.addMutation(m4);
            writer.addMutation(m5);
            writer.addMutation(m6);
            writer.addMutation(m7);
            writer.addMutation(m8);
            writer.addMutation(m9);
            writer.close();

            // Read data back and check we get one merged element
            final Authorizations authorizations = new Authorizations(visibilityString);
            final BatchScanner scanner = store.getMockConnector().createBatchScanner(store.getProperties().getTable(), authorizations, 1000);
            try {
                scanner.addScanIterator(store.getKeyPackage().getIteratorFactory().getRowIDAggregatorIteratorSetting(store, "BasicEdge2"));
            } catch (IteratorSettingException e) {
                fail(e.getMessage());
            }
            RangeFactory rangeF = store.getKeyPackage().getRangeFactory();
            Range r = rangeF.getRangeFromPair(new Pair<ElementSeed>((new EntitySeed("1")), new EntitySeed("4")), new SummariseGroupOverRanges());
            Range r2 = rangeF.getRangeFromPair(new Pair<ElementSeed>((new EntitySeed("5")), new EntitySeed("5")), new SummariseGroupOverRanges());
            scanner.setRanges(Arrays.asList(r, r2));
            final Iterator<Map.Entry<Key, Value>> it = scanner.iterator();
            Map.Entry<Key, Value> entry = it.next();
            Element readEdge = elementConverter.getFullElement(entry.getKey(), entry.getValue());

            Edge expectedEdge = new Edge("BasicEdge2");
            expectedEdge.setSource("4");
            expectedEdge.setDestination("8");
            expectedEdge.setDirected(true);
            expectedEdge.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 5);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_1, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_2, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_3, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_4, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.COUNT, 3);

            assertEquals(expectedEdge, readEdge);
            assertEquals(5, readEdge.getProperty(AccumuloPropertyNames.COLUMN_QUALIFIER));
            assertEquals(3, readEdge.getProperty(AccumuloPropertyNames.COUNT));
            // Check no more entries
            assertTrue(it.hasNext());
            entry = it.next();
            readEdge = elementConverter.getFullElement(entry.getKey(), entry.getValue());
            expectedEdge = new Edge("BasicEdge2");
            expectedEdge.setSource("5");
            expectedEdge.setDestination("9");
            expectedEdge.setDirected(true);
            expectedEdge.putProperty(AccumuloPropertyNames.COLUMN_QUALIFIER, 2);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_1, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_2, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_3, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.PROP_4, 0);
            expectedEdge.putProperty(AccumuloPropertyNames.COUNT, 1);
            assertEquals(expectedEdge, readEdge);
            if (it.hasNext()) {
                fail("Additional row found.");
            }
        } catch (AccumuloException | TableExistsException | TableNotFoundException e) {
            fail(this.getClass().getSimpleName() + " failed with exception: " + e);
        }
    }

}