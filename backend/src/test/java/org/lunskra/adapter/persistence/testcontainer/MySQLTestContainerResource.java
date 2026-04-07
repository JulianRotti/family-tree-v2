package org.lunskra.adapter.persistence.testcontainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

public class MySQLTestContainerResource implements QuarkusTestResourceLifecycleManager {

    static final MySQLContainer DB = new MySQLContainer(
            DockerImageName.parse("mysql:8.4")
    )
            .withDatabaseName("family_tree")
            .withUsername("dev_user")
            .withPassword("password")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/init.sql"),
                    "/docker-entrypoint-initdb.d/01-init.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("db/fillTestdata.sql"),
                    "/docker-entrypoint-initdb.d/02-fillTestdata.sql"
            )
            // CSV files go to mysql's import dir for LOAD DATA INFILE
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("testdata/test_data_members.csv"),
                    "/var/lib/mysql-files/test_data_members.csv"
            )
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("testdata/test_data_relationships.csv"),
                    "/var/lib/mysql-files/test_data_relationships.csv"
            );;

    @Override
    public Map<String, String> start() {
        DB.start();
        System.out.println("STARTED: " + DB.getJdbcUrl());
        return Map.of(
                "quarkus.datasource.jdbc.url", DB.getJdbcUrl(),
                "quarkus.datasource.username", DB.getUsername(),
                "quarkus.datasource.password", DB.getPassword());
    }

    @Override
    public void stop() {
        DB.stop();
    }
}
