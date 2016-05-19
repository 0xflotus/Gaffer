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
package gaffer.example.operation;

import gaffer.data.element.function.ElementFilter;
import gaffer.data.elementdefinition.view.View;
import gaffer.data.elementdefinition.view.ViewElementDefinition;
import gaffer.function.simple.filter.IsMoreThan;
import gaffer.graph.Graph;
import gaffer.operation.GetOperation.IncludeIncomingOutgoingType;
import gaffer.operation.OperationException;
import gaffer.operation.data.EntitySeed;
import gaffer.operation.impl.get.GetAdjacentEntitySeeds;

public class GetAdjacentEntitySeedsExample extends OperationExample {
    public static void main(final String[] args) throws OperationException {
        new GetAdjacentEntitySeedsExample().run();
    }

    public GetAdjacentEntitySeedsExample() {
        super(GetAdjacentEntitySeeds.class);
    }

    public void runExamples(final Graph graph) throws OperationException {
        getAdjacentEntitySeedsFromVertex2(graph);
        getAdjacentEntitySeedsAlongOutboundEdgesFromVertex2(graph);
        getAdjacentEntitySeedsAlongOutboundEdgesFromVertex2WithCountGreaterThan1(graph);
    }

    public Iterable<EntitySeed> getAdjacentEntitySeedsFromVertex2(final Graph graph) throws OperationException {
        return runAndPrintOperation(new GetAdjacentEntitySeeds.Builder()
                .addSeed(new EntitySeed(2))
                .build(), graph);
    }

    public Iterable<EntitySeed> getAdjacentEntitySeedsAlongOutboundEdgesFromVertex2(final Graph graph) throws OperationException {
        return runAndPrintOperation(new GetAdjacentEntitySeeds.Builder()
                .addSeed(new EntitySeed(2))
                .inOutType(IncludeIncomingOutgoingType.OUTGOING)
                .build(), graph);
    }

    public Iterable<EntitySeed> getAdjacentEntitySeedsAlongOutboundEdgesFromVertex2WithCountGreaterThan1(final Graph graph) throws OperationException {
        return runAndPrintOperation(new GetAdjacentEntitySeeds.Builder()
                .addSeed(new EntitySeed(2))
                .inOutType(IncludeIncomingOutgoingType.OUTGOING)
                .view(new View.Builder()
                        .entity("entity", new ViewElementDefinition.Builder()
                                .filter(new ElementFilter.Builder()
                                        .select(COUNT)
                                        .execute(new IsMoreThan(1))
                                        .build())
                                .build())
                        .edge("edge", new ViewElementDefinition.Builder()
                                .filter(new ElementFilter.Builder()
                                        .select(COUNT)
                                        .execute(new IsMoreThan(1))
                                        .build())
                                .build())
                        .build())
                .build(), graph);
    }
}
