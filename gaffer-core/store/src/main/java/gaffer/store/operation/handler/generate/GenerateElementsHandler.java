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

package gaffer.store.operation.handler.generate;

import gaffer.commonutil.iterable.CloseableIterable;
import gaffer.commonutil.iterable.WrappedCloseableIterable;
import gaffer.data.element.Element;
import gaffer.operation.OperationException;
import gaffer.operation.impl.generate.GenerateElements;
import gaffer.store.Context;
import gaffer.store.Store;
import gaffer.store.operation.handler.OperationHandler;

/**
 * An <code>GenerateElementsHandler</code> handles {@link gaffer.operation.impl.generate.GenerateElements} operations.
 * It uses the {@link gaffer.data.generator.ElementGenerator} from the operation to generate
 * {@link gaffer.data.element.Element}s from the operation input objects.
 *
 * @param <OBJ> the type of input objects from the operation.
 */
public class GenerateElementsHandler<OBJ> implements OperationHandler<GenerateElements<OBJ>, CloseableIterable<Element>> {
    @Override
    public CloseableIterable<Element> doOperation(final GenerateElements<OBJ> operation,
                                                  final Context context, final Store store)
            throws OperationException {
        return new WrappedCloseableIterable<>(operation.getElementGenerator().getElements(operation.getObjects()));
    }
}
