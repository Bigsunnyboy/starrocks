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

package com.starrocks.sql.ast;

import com.starrocks.catalog.Column;
import com.starrocks.catalog.PrimitiveType;
import com.starrocks.catalog.ScalarType;
import com.starrocks.qe.ShowResultSetMetaData;
import com.starrocks.sql.parser.NodePosition;

// SHOW PROCESSLIST statement.
// Used to show connection belong to this user.
public class ShowProcesslistStmt extends ShowStmt {
    private static final ShowResultSetMetaData META_DATA =
            ShowResultSetMetaData.builder()
                    .addColumn(new Column("ServerName", ScalarType.createVarchar(64)))
                    .addColumn(new Column("Id", ScalarType.createType(PrimitiveType.BIGINT)))
                    .addColumn(new Column("User", ScalarType.createVarchar(16)))
                    .addColumn(new Column("Host", ScalarType.createVarchar(16)))
                    .addColumn(new Column("Db", ScalarType.createVarchar(16)))
                    .addColumn(new Column("Command", ScalarType.createVarchar(16)))
                    .addColumn(new Column("ConnectionStartTime", ScalarType.createVarchar(16)))
                    .addColumn(new Column("Time", ScalarType.createType(PrimitiveType.INT)))
                    .addColumn(new Column("State", ScalarType.createVarchar(64)))
                    .addColumn(new Column("Info", ScalarType.createVarchar(32 * 1024)))
                    .addColumn(new Column("IsPending", ScalarType.createVarchar(16)))
                    .addColumn(new Column("Warehouse", ScalarType.createVarchar(20)))
                    .addColumn(new Column("CNGroup", ScalarType.createVarchar(64)))
                    .build();
    private final boolean isShowFull;
    private final String forUser;

    public ShowProcesslistStmt(boolean isShowFull) {
        this(isShowFull, null, NodePosition.ZERO);
    }

    public ShowProcesslistStmt(boolean isShowFull, String forUser, NodePosition pos) {
        super(pos);
        this.isShowFull = isShowFull;
        this.forUser = forUser;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitShowProcesslistStatement(this, context);
    }

    @Override
    public ShowResultSetMetaData getMetaData() {
        return META_DATA;
    }

    public boolean showFull() {
        return isShowFull;
    }

    public String getForUser() {
        return forUser;
    }
}
