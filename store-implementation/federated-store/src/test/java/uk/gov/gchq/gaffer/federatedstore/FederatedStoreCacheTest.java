/*
 * Copyright 2017 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.gaffer.cache.exception.CacheOperationException;
import uk.gov.gchq.gaffer.commonutil.StreamUtil;
import uk.gov.gchq.gaffer.commonutil.exception.OverwritingException;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.graph.GraphConfig;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FederatedStoreCacheTest {
    private static final String PATH_MAP_STORE_PROPERTIES = "properties/singleUseMockMapStore.properties";
    private static final String PATH_BASIC_EDGE_SCHEMA_JSON = "schema/basicEdgeSchema.json";
    private static final String MAP_ID_1 = "mockMapGraphId1";
    private Graph testGraph = new Graph.Builder().config(new GraphConfig(MAP_ID_1))
            .storeProperties(StreamUtil.openStream(FederatedStoreTest.class, PATH_MAP_STORE_PROPERTIES))
            .addSchema(StreamUtil.openStream(FederatedStoreTest.class, PATH_BASIC_EDGE_SCHEMA_JSON))
            .build();
    private static FederatedStoreCache federatedStoreCache;

    @BeforeClass
    public static void setUp() {
        federatedStoreCache = new FederatedStoreCache();
    }

    @Before
    public void beforeEach() throws CacheOperationException {
        federatedStoreCache.clearCache();
    }

    @Test
    public void shouldAddAndGetGraphToCache() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        Graph cached = federatedStoreCache.getFromCache(MAP_ID_1);

        assertEquals(testGraph.getGraphId(), cached.getGraphId());
        assertEquals(testGraph.getSchema().toString(), cached.getSchema().toString());
        assertEquals(testGraph.getStoreProperties(), cached.getStoreProperties());
    }

    @Test
    public void shouldGetAllGraphIdsFromCache() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        Set<String> cachedGraphIds = federatedStoreCache.getAllGraphIds();
        assertEquals(1, cachedGraphIds.size());
        assertTrue(cachedGraphIds.contains(testGraph.getGraphId()));
    }

    @Test
    public void shouldDeleteFromCache() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        Set<String> cachedGraphIds = federatedStoreCache.getAllGraphIds();
        assertEquals(1, cachedGraphIds.size());
        assertTrue(cachedGraphIds.contains(testGraph.getGraphId()));

        federatedStoreCache.deleteFromCache(testGraph.getGraphId());
        Set<String> cachedGraphIdsAfterDelete = federatedStoreCache.getAllGraphIds();
        assertEquals(0, cachedGraphIdsAfterDelete.size());
    }

    @Test
    public void shouldThrowExceptionIfGraphAlreadyExistsInCache() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        try {
            federatedStoreCache.addGraphToCache(testGraph, false);
            fail("Exception expected");
        } catch (OverwritingException e) {
            assertTrue(e.getMessage().contains("Cache entry already exists"));
        }
    }

    @Test
    public void shouldThrowExceptionIfGraphIdToBeRemovedIsNull() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        try {
            federatedStoreCache.deleteFromCache(null);
        } catch (CacheOperationException e) {
            assertTrue(e.getMessage().contains("Graph ID cannot be null"));
        }
    }

    @Test
    public void shouldThrowExceptionIfGraphIdToGetIsNull() throws CacheOperationException {
        federatedStoreCache.addGraphToCache(testGraph, false);
        try {
            federatedStoreCache.getFromCache(null);
        } catch (CacheOperationException e) {
            assertTrue(e.getMessage().contains("Graph ID cannot be null"));
        }
    }
}
