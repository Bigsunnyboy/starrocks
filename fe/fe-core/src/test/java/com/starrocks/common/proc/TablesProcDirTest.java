// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.starrocks.common.proc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.starrocks.catalog.Column;
import com.starrocks.catalog.Database;
import com.starrocks.catalog.HudiTable;
import com.starrocks.catalog.ListPartitionInfo;
import com.starrocks.catalog.OlapTable;
import com.starrocks.catalog.Partition;
import com.starrocks.catalog.PartitionInfo;
import com.starrocks.catalog.PartitionType;
import com.starrocks.catalog.RangePartitionInfo;
import com.starrocks.catalog.Table;
import com.starrocks.catalog.Type;
import com.starrocks.common.AnalysisException;
import com.starrocks.common.DdlException;
import com.starrocks.common.FeConstants;
import com.starrocks.server.LocalMetastore;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


public class TablesProcDirTest {
    private Database db;

    @BeforeEach
    public void setUp() throws DdlException, AnalysisException {
        db = new Database(10000L, "db1");
        Map<String, Long> indexNameToId = Maps.newHashMap();
        indexNameToId.put("index1", 1000L);

        List<Partition> p1 = Lists.newArrayList(new Partition(1001L, 1011L, "p", null, null));
        List<Column> col1 = Lists.newArrayList(new Column("province", Type.VARCHAR));
        PartitionInfo pt1 = new ListPartitionInfo(PartitionType.LIST, col1);
        OlapTable tb1 = new OlapTable(1000L, "tb1", col1, null, pt1, null);
        new Expectations(tb1) {
            {
                tb1.getIndexNameToId();
                minTimes = 0;
                result = indexNameToId;

                tb1.getPartitions();
                minTimes = 0;
                result = p1;

                tb1.getReplicaCount();
                minTimes = 0;
                result = 2;
            }
        };

        List<Partition> p2 = Lists.newArrayList(new Partition(20001L, 20011L, "p", null, null));
        List<Column> col2 = Lists.newArrayList(new Column("dt", Type.DATE));
        PartitionInfo pt2 = new RangePartitionInfo(col2);
        OlapTable tb2 = new OlapTable(2000L, "tb2", col2, null, pt2, null);
        new Expectations(tb2) {
            {
                tb2.getIndexNameToId();
                minTimes = 0;
                result = indexNameToId;

                tb2.getPartitions();
                minTimes = 0;
                result = p2;

                tb2.getReplicaCount();
                minTimes = 0;
                result = 2;
            }
        };

        //long id, String name, List<Column> schema, Map<String, String> properties
        HudiTable tb3 = new HudiTable();
        new Expectations(tb3) {
            {
                tb3.getName();
                minTimes = 0;
                result = "tb3";

                tb3.getId();
                minTimes = 0;
                result = 3000L;
            }
        };

        db.registerTableUnlocked(tb1);
        db.registerTableUnlocked(tb2);
        db.registerTableUnlocked(tb3);

        new MockUp<LocalMetastore>() {
            @Mock
            public Database getDb(String dbName) {
                return db;
            }

            @Mock
            public Table getTable(String dbName, String tblName) {
                return db.getTable(tblName);
            }

            @Mock
            public Table getTable(Long dbId, Long tableId) {
                return db.getTable(tableId);
            }

            @Mock
            public List<Table> getTables(Long dbId) {
                return db.getTables();
            }
        };
    }

    @Test
    public void testFetchResult() throws AnalysisException {
        BaseProcResult result = (BaseProcResult) new TablesProcDir(db).fetchResult();
        List<List<String>> rows = result.getRows();
        List<String> list1 = rows.get(0);
        Assertions.assertEquals(list1.size(), TablesProcDir.TITLE_NAMES.size());
        // TableId
        Assertions.assertEquals("1000", list1.get(0));
        // TableName
        Assertions.assertEquals("tb1", list1.get(1));
        // IndexNum
        Assertions.assertEquals("1", list1.get(2));
        // PartitionColumnName
        Assertions.assertEquals("province", list1.get(3));
        // PartitionNum
        Assertions.assertEquals("1", list1.get(4));
        // State
        Assertions.assertEquals("NORMAL", list1.get(5));
        // Type
        Assertions.assertEquals("OLAP", list1.get(6));
        // LastConsistencyCheckTime
        Assertions.assertEquals(FeConstants.NULL_STRING, list1.get(7));
        // ReplicaCount
        Assertions.assertEquals("2", list1.get(8));
        // PartitionType
        Assertions.assertEquals("LIST", list1.get(9));

        List<String> list2 = rows.get(1);
        Assertions.assertEquals(list2.size(), TablesProcDir.TITLE_NAMES.size());
        // TableId
        Assertions.assertEquals("2000", list2.get(0));
        // TableName
        Assertions.assertEquals("tb2", list2.get(1));
        // IndexNum
        Assertions.assertEquals("1", list2.get(2));
        // PartitionColumnName
        Assertions.assertEquals("dt", list2.get(3));
        // PartitionNum
        Assertions.assertEquals("1", list2.get(4));
        // State
        Assertions.assertEquals("NORMAL", list2.get(5));
        // Type
        Assertions.assertEquals("OLAP", list2.get(6));
        // LastConsistencyCheckTime
        Assertions.assertEquals(FeConstants.NULL_STRING, list2.get(7));
        // ReplicaCount
        Assertions.assertEquals("2", list2.get(8));
        // PartitionType
        Assertions.assertEquals("RANGE", list2.get(9));

        List<String> list3 = rows.get(2);
        Assertions.assertEquals(list2.size(), TablesProcDir.TITLE_NAMES.size());
        // TableId
        Assertions.assertEquals("3000", list3.get(0));
        // TableName
        Assertions.assertEquals("tb3", list3.get(1));
        // IndexNum
        Assertions.assertEquals(FeConstants.NULL_STRING, list3.get(2));
        // PartitionColumnName
        Assertions.assertEquals(FeConstants.NULL_STRING, list3.get(3));
        // PartitionNum
        Assertions.assertEquals("1", list3.get(4));
        // State
        Assertions.assertEquals(FeConstants.NULL_STRING, list3.get(5));
        // Type
        Assertions.assertEquals("HUDI", list3.get(6));
        // LastConsistencyCheckTime
        Assertions.assertEquals(FeConstants.NULL_STRING, list3.get(7));
        // ReplicaCount
        Assertions.assertEquals("0", list3.get(8));
        // PartitionType
        Assertions.assertEquals("UNPARTITIONED", list3.get(9));

    }


}
