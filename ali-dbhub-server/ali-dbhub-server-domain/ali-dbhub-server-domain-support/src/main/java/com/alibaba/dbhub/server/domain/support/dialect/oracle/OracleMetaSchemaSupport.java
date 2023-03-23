/**
 * alibaba.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.alibaba.dbhub.server.domain.support.dialect.oracle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.dbhub.server.domain.support.dialect.MetaSchema;
import com.alibaba.dbhub.server.domain.support.dialect.oracle.mapper.OracleMetaSchemaMapper;
import com.alibaba.dbhub.server.domain.support.enums.DbTypeEnum;
import com.alibaba.dbhub.server.domain.support.model.Function;
import com.alibaba.dbhub.server.domain.support.model.Procedure;
import com.alibaba.dbhub.server.domain.support.model.Table;
import com.alibaba.dbhub.server.domain.support.model.TableColumn;
import com.alibaba.dbhub.server.domain.support.model.TableIndex;
import com.alibaba.dbhub.server.domain.support.model.TableIndexColumn;
import com.alibaba.dbhub.server.domain.support.model.TableIndexColumnUnion;
import com.alibaba.dbhub.server.domain.support.model.Trigger;
import com.alibaba.dbhub.server.domain.support.sql.DbhubDataSource;
import com.alibaba.dbhub.server.tools.common.util.EasyCollectionUtils;

import com.google.common.collect.Lists;

/**
 * @author jipengfei
 * @version : OracleMetaSchemaSupport.java
 */
public class OracleMetaSchemaSupport implements MetaSchema<Table> {

    private OracleMetaSchemaMapper getMapper() {
        return DbhubDataSource.getInstance().getMapper(OracleMetaSchemaMapper.class);
    }

    @Override
    public DbTypeEnum dbType() {
        return DbTypeEnum.ORACLE;
    }

    @Override
    public List<String> databases() {
        return getMapper().showDatabases();
    }

    @Override
    public List<String> schemas(String databaseName) {
        return null;
    }

    @Override
    public String tableDDL(String databaseName, String schemaName, String tableName) {
        return getMapper().showCreateTable(schemaName, tableName);
    }

    @Override
    public void dropTable(String databaseName, String schemaName, String tableName) {
        getMapper().dropTable(tableName);
    }

    @Override
    public List<Table> tables(String databaseName, String schemaName) {
        return getMapper().selectTables(databaseName);
    }

    @Override
    public List<Table> views(String databaseName, String schemaName) {
        return null;
    }

    @Override
    public List<Function> functions(String databaseName, String schemaName) {
        return null;
    }

    @Override
    public List<Trigger> triggers(String databaseName, String schemaName) {
        return null;
    }

    @Override
    public List<Procedure> procedures(String databaseName, String schemaName) {
        return null;
    }

    @Override
    public List<? extends TableIndex> indexes(String databaseName, String schemaName, String tableName) {
        List<TableIndex> dataList = Lists.newArrayList();
        List<TableIndexColumnUnion> list = getMapper().selectTableIndexes(databaseName, tableName);
        Map<String, List<TableIndexColumnUnion>> map = list.stream().collect(
            Collectors.groupingBy(TableIndexColumnUnion::getIndexName));
        for (Map.Entry<String, List<TableIndexColumnUnion>> entry1 : map.entrySet()) {
            TableIndexColumnUnion first = entry1.getValue().get(0);
            dataList.add(buildTableIndex(tableName, entry1.getKey(), first.getType(), first.getComment(),
                entry1.getValue()));
        }
        //按列分组
        return dataList;
    }

    private TableIndex buildTableIndex(String tableName, String indexName, String type, String comment,
        List<TableIndexColumnUnion> indexSubList) {
        return TableIndex.builder()
            .tableName(tableName)
            .name(indexName)
            .type(type)
            .comment(comment)
            .columnList(buildIndexColumn(indexSubList, indexName, tableName))
            .build();
    }

    private List<TableIndexColumn> buildIndexColumn(List<TableIndexColumnUnion> indexSubList, String indexName,
        String tableName) {
        return EasyCollectionUtils.toList(indexSubList, indexColumnUnion -> TableIndexColumn.builder()
            .name(indexColumnUnion.getColumnName())
            .indexName(indexName)
            .tableName(tableName)
            .collation(indexColumnUnion.getCollation())
            .build());

    }

    @Override
    public List<? extends TableColumn> columns(String databaseName, String schemaName, String tableName) {
        return getMapper().selectColumns(tableName);
    }
}