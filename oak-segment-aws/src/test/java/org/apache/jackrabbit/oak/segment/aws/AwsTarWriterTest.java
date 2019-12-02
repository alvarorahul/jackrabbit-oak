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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.jackrabbit.oak.segment.spi.monitor.IOMonitorAdapter;
import org.apache.jackrabbit.oak.segment.file.tar.TarWriterTest;
import org.apache.jackrabbit.oak.segment.spi.monitor.RemoteStoreMonitorAdapter;
import org.junit.Before;
import org.junit.ClassRule;

import java.io.IOException;
import java.util.Date;

public class AwsTarWriterTest extends TarWriterTest {

    @ClassRule
    public static final S3MockRule s3Mock = new S3MockRule();

    @Before
    @Override
    public void setUp() throws IOException {
        String bucketName = "testbucket-" + new Date().getTime();
        AmazonS3 s3 = s3Mock.createClient();
        s3.createBucket(bucketName);

        String tableName = "testtable-" + new Date().getTime();
        String lockTableName = "locktable-" + new Date().getTime();
        AmazonDynamoDB ddb = DynamoDBMock.createClient(tableName, lockTableName);

        AwsContext awsContext = AwsContext.create(s3, bucketName, "oak", ddb, tableName, lockTableName);

        monitor = new TestFileStoreMonitor();
        archiveManager = new AwsPersistence(awsContext).createArchiveManager(true, false, new IOMonitorAdapter(),
                monitor, new RemoteStoreMonitorAdapter());
    }
}
