/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.segment.aws;

import java.io.IOException;

import org.apache.jackrabbit.oak.segment.spi.monitor.FileStoreMonitor;
import org.apache.jackrabbit.oak.segment.spi.monitor.IOMonitor;
import org.apache.jackrabbit.oak.segment.spi.monitor.RemoteStoreMonitor;
import org.apache.jackrabbit.oak.segment.spi.persistence.GCJournalFile;
import org.apache.jackrabbit.oak.segment.spi.persistence.JournalFile;
import org.apache.jackrabbit.oak.segment.spi.persistence.ManifestFile;
import org.apache.jackrabbit.oak.segment.spi.persistence.RepositoryLock;
import org.apache.jackrabbit.oak.segment.spi.persistence.SegmentArchiveManager;
import org.apache.jackrabbit.oak.segment.spi.persistence.SegmentNodeStorePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsPersistence implements SegmentNodeStorePersistence {

    // private static int RETRY_ATTEMPTS = Integer.getInteger("segment.aws.retry.attempts", 5);

    // private static int RETRY_BACKOFF_SECONDS = Integer.getInteger("segment.aws.retry.backoff", 5);

    // private static int TIMEOUT_EXECUTION = Integer.getInteger("segment.timeout.execution", 30);

    // private static int TIMEOUT_INTERVAL = Integer.getInteger("segment.timeout.interval", 1);

    private static final Logger log = LoggerFactory.getLogger(AwsPersistence.class);

    protected final AwsContext awsContext;

    public AwsPersistence(AwsContext awsContext) {
        this.awsContext = awsContext;

        // TODO assign request options from config

        // BlobRequestOptions defaultRequestOptions = segmentStoreDirectory.getServiceClient().getDefaultRequestOptions();
        // if (defaultRequestOptions.getRetryPolicyFactory() == null) {
        //     if (RETRY_ATTEMPTS > 0) {
        //         defaultRequestOptions.setRetryPolicyFactory(
        //                 new RetryLinearRetry((int) TimeUnit.SECONDS.toMillis(RETRY_BACKOFF_SECONDS), RETRY_ATTEMPTS));
        //     }
        // }
        // if (defaultRequestOptions.getMaximumExecutionTimeInMs() == null) {
        //     if (TIMEOUT_EXECUTION > 0) {
        //         defaultRequestOptions.setMaximumExecutionTimeInMs((int) TimeUnit.SECONDS.toMillis(TIMEOUT_EXECUTION));
        //     }
        // }
        // if (defaultRequestOptions.getTimeoutIntervalInMs() == null) {
        //     if (TIMEOUT_INTERVAL > 0) {
        //         defaultRequestOptions.setTimeoutIntervalInMs((int) TimeUnit.SECONDS.toMillis(TIMEOUT_INTERVAL));
        //     }
        // }
    }

    @Override
    public SegmentArchiveManager createArchiveManager(boolean mmap, boolean offHeapAccess, IOMonitor ioMonitor,
            FileStoreMonitor fileStoreMonitor, RemoteStoreMonitor remoteStoreMonitor) {
        attachRemoteStoreMonitor(remoteStoreMonitor);
        return new AwsArchiveManager(awsContext, ioMonitor, fileStoreMonitor);
    }

    @Override
    public boolean segmentFilesExist() {
        try {
            for (String prefix : awsContext.listPrefixes()) {
                if (prefix.indexOf(".tar/") >= 0) {
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            log.error("Can't check if the segment archives exists", e);
            return false;
        }
    }

    @Override
    public JournalFile getJournalFile() {
        return new AwsJournalFile(awsContext, "journal.log");
    }

    @Override
    public GCJournalFile getGCJournalFile() throws IOException {
        return new AwsGCJournalFile(awsContext, "gc.log");
    }

    @Override
    public ManifestFile getManifestFile() throws IOException {
        return new AwsManifestFile(awsContext, "manifest");
    }

    @Override
    public RepositoryLock lockRepository() throws IOException {
        return new AwsRepositoryLock(awsContext, "repo.lock", () -> {
            log.warn("Lost connection to AWS. The client will be closed.");
            // TODO close the connection
        }).lock();
    }

    private static void attachRemoteStoreMonitor(RemoteStoreMonitor remoteStoreMonitor) {
        // TODO add support for remote store monitor.
        // OperationContext.getGlobalRequestCompletedEventHandler().addListener(new
        // StorageEvent<RequestCompletedEvent>() {

        // @Override
        // public void eventOccurred(RequestCompletedEvent e) {
        // Date startDate = e.getRequestResult().getStartDate();
        // Date stopDate = e.getRequestResult().getStopDate();

        // if (startDate != null && stopDate != null) {
        // long requestDuration = stopDate.getTime() - startDate.getTime();
        // remoteStoreMonitor.requestDuration(requestDuration, TimeUnit.MILLISECONDS);
        // }

        // Exception exception = e.getRequestResult().getException();

        // if (exception == null) {
        // remoteStoreMonitor.requestCount();
        // } else {
        // remoteStoreMonitor.requestError();
        // }
        // }

        // });
    }

    // public CloudBlobDirectory getSegmentstoreDirectory() {
    // return segmentstoreDirectory;
    // }
}
