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

package gaffer.accumulostore.operation.handler;

import gaffer.accumulostore.AccumuloStore;
import gaffer.accumulostore.key.IteratorSettingFactory;
import gaffer.accumulostore.key.exception.IteratorSettingException;
import gaffer.accumulostore.operation.impl.GetElementsInRanges;
import gaffer.accumulostore.retriever.impl.AccumuloRangeIDRetriever;
import gaffer.accumulostore.utils.Pair;
import gaffer.data.element.Element;
import gaffer.operation.OperationException;
import gaffer.operation.data.ElementSeed;
import gaffer.store.Context;
import gaffer.store.Store;
import gaffer.store.StoreException;
import gaffer.store.operation.handler.OperationHandler;
import gaffer.user.User;

public class GetElementsInRangesHandler
        implements OperationHandler<GetElementsInRanges<Pair<ElementSeed>, Element>, Iterable<Element>> {

    @Override
    public Iterable<Element> doOperation(final GetElementsInRanges<Pair<ElementSeed>, Element> operation,
                                         final Context context, final Store store)
            throws OperationException {
        return doOperation(operation, context.getUser(), (AccumuloStore) store);
    }

    public Iterable<Element> doOperation(final GetElementsInRanges<Pair<ElementSeed>, Element> operation,
                                         final User user,
                                         final AccumuloStore store) throws OperationException {
        final IteratorSettingFactory itrFactory = store.getKeyPackage().getIteratorFactory();
        try {
            return new AccumuloRangeIDRetriever(store, operation, user,
                    itrFactory.getElementPreAggregationFilterIteratorSetting(operation.getView(), store),
                    itrFactory.getElementPostAggregationFilterIteratorSetting(operation.getView(), store),
                    itrFactory.getEdgeEntityDirectionFilterIteratorSetting(operation),
                    itrFactory.getElementPropertyRangeQueryFilter(operation),
                    itrFactory.getQueryTimeAggregatorIteratorSetting(operation.getView(), store));
        } catch (IteratorSettingException | StoreException e) {
            throw new OperationException("Failed to get elements", e);
        }
    }
}
