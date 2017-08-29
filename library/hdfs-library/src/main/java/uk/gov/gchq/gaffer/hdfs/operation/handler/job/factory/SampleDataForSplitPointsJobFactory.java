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
package uk.gov.gchq.gaffer.hdfs.operation.handler.job.factory;

import com.google.common.collect.Lists;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import uk.gov.gchq.gaffer.hdfs.operation.SampleDataForSplitPoints;
import uk.gov.gchq.gaffer.store.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SampleDataForSplitPointsJobFactory extends JobFactory<SampleDataForSplitPoints> {
    String PROPORTION_TO_SAMPLE = "proportion_to_sample";

    @Override
    default List<Job> createJobs(final SampleDataForSplitPoints operation, final Store store) throws IOException {
        final List<Job> jobs = new ArrayList<>();
        Map<String, List<String>> mapperGeneratorsToInputPathsList = new HashMap<>();
        for (final Map.Entry<String, String> entry : operation.getInputMapperPairs().entrySet()) {
            if (mapperGeneratorsToInputPathsList.containsKey(entry.getValue())) {
                mapperGeneratorsToInputPathsList.get(entry.getValue()).add(entry.getKey());
            } else {
                mapperGeneratorsToInputPathsList.put(entry.getValue(), Lists.newArrayList(entry.getKey()));
            }
        }

        for (final String mapperGeneratorClassName : mapperGeneratorsToInputPathsList.keySet()) {
            final JobConf jobConf = createJobConf(operation, mapperGeneratorClassName, store);
            final Job job = Job.getInstance(jobConf);
            setupJob(job, operation, mapperGeneratorClassName, store);

            if (null != operation.getJobInitialiser()) {
                operation.getJobInitialiser().initialiseJob(job, operation, store);
            }
            jobs.add(job);
        }
        return jobs;
    }

    Writable createKey();

    Writable createValue();

    byte[] createSplit(final Writable key, final Writable value);

    int getExpectedNumberOfSplits(final Store store);
}
