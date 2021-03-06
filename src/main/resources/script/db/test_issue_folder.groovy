package script.db
databaseChangeLog(logicalFilePath: 'script/script/init_tables.groovy.groovy') {
    changeSet(author: 'zongwei.li@hang-china.com', id: '2018-08-30-add-test_issue_folder') {
        createTable(tableName: "test_issue_folder") {
            column(name: 'folder_id', type: 'BIGINT UNSIGNED', autoIncrement: true, remarks: '表ID，主键，供其他表做外键，unsigned bigint、单表时自增、步长为 1') {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(100)', remarks: '文件夹名称') {
                constraints(nullable: false)
            }
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '所属版本') {
                constraints(nullable: false)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '所属项目') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(10)', remarks: '类型') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: 'uk_version_id_project_id', tableName: 'test_issue_folder', unique: true) {
            column(name: 'project_id')
            column(name: 'version_id')
            column(name: 'name')
        }
    }
    changeSet(author: 'jialongzuo@hang-china.com', id: '2018-09-18-add_sequence_test_issue_folder') {

        if (helper.dbType().isSupportSequence()) {
            createSequence(sequenceName: 'test_issue_folder_s', startValue: "1")
        }
    }
}