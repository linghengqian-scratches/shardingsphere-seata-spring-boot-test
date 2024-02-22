package com.lingh.shardingsphereseataspringboottest;

import com.lingh.shardingsphereseataspringboottest.commons.TestShardingService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Stream;

@SpringBootTest
@SuppressWarnings({"deprecation", "resource"})
class SeataAtTest {

    @Autowired
    private DataSource dataSource;

    private TestShardingService testShardingService;

    @Test
    void assertShardingInSeataTransactions() throws SQLException {
        try (
                GenericContainer<?> container = new FixedHostPortGenericContainer<>("seataio/seata-server:2.0.0")
                        .withFixedExposedPort(37403, 8091)
                        .withExposedPorts(7091)) {
            container.start();
            initDataSource(container.getMappedPort(7091));
            testShardingService = new TestShardingService(dataSource);
            this.initEnvironment();
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }

    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInPostgres();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInPostgres();
        testShardingService.getAddressRepository().createTableIfNotExists();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }

    private Connection openConnection(final String databaseName) throws SQLException {
        final String jdbcUrlPrefix = "jdbc:tc:postgresql:16.2-bookworm://test-native-transactions-base/";
        return DriverManager.getConnection(jdbcUrlPrefix + databaseName + "?TC_DAEMON=true", new Properties());
    }

    private void initDataSource(final Integer seataServerHealthCheckPort) {
        Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptions()
                .until(() -> verifySeataServerRunning(seataServerHealthCheckPort));
        Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptions()
                .until(() -> {
                    openConnection("demo_ds_0").close();
                    openConnection("demo_ds_1").close();
                    openConnection("demo_ds_2").close();
                    return true;
                });
        final String firstSql = """
                CREATE TABLE IF NOT EXISTS public.undo_log
                (
                    id            SERIAL       NOT NULL,
                    branch_id     BIGINT       NOT NULL,
                    xid           VARCHAR(128) NOT NULL,
                    context       VARCHAR(128) NOT NULL,
                    rollback_info BYTEA        NOT NULL,
                    log_status    INT          NOT NULL,
                    log_created   TIMESTAMP(0) NOT NULL,
                    log_modified  TIMESTAMP(0) NOT NULL,
                    CONSTRAINT pk_undo_log PRIMARY KEY (id),
                    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
                );""";
        final String secondSql = "CREATE INDEX ix_log_created ON undo_log(log_created);";
        final String thirdSql = "COMMENT ON TABLE public.undo_log IS 'AT transaction mode undo table';";
        final String fourthSql = "COMMENT ON COLUMN public.undo_log.branch_id IS 'branch transaction id';";
        final String fifthSql = "COMMENT ON COLUMN public.undo_log.xid IS 'global transaction id';";
        final String sixthSql = "COMMENT ON COLUMN public.undo_log.context IS 'undo_log context,such as serialization';";
        final String seventhSql = "COMMENT ON COLUMN public.undo_log.rollback_info IS 'rollback info';";
        final String eighthSql = "COMMENT ON COLUMN public.undo_log.log_status IS '0:normal status,1:defense status';";
        final String ninthSql = "COMMENT ON COLUMN public.undo_log.log_created IS 'create datetime';";
        final String tenthSql = "COMMENT ON COLUMN public.undo_log.log_modified IS 'modify datetime';";
        final String eleventhSql = "CREATE SEQUENCE IF NOT EXISTS undo_log_id_seq INCREMENT BY 1 MINVALUE 1 ;";
        Stream.of(firstSql, secondSql, thirdSql, fourthSql, fifthSql, sixthSql, seventhSql, eighthSql, ninthSql, tenthSql, eleventhSql)
                .forEachOrdered(this::executeSqlToShardingDB);
    }

    /**
     * Further processing of this class awaits <a href="https://github.com/apache/incubator-seata/pull/6356">apache/incubator-seata#6356</a>.
     *
     * @param seataServerHealthCheckPort The port of the host corresponding to port `7091` inside the container
     * @return Returns true if Seata Server is running normally.
     * @throws IOException Signals that an I/O exception to some sort has occurred.
     */
    private static boolean verifySeataServerRunning(final Integer seataServerHealthCheckPort) throws IOException {
        boolean flag = false;
        HttpGet httpGet = new HttpGet("http://localhost:" + seataServerHealthCheckPort + "/health");
        try (
                CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(httpGet)) {
            if (HttpStatus.SC_UNAUTHORIZED == response.getCode()) {
                flag = true;
            }
            EntityUtils.consume(response.getEntity());
        }
        return flag;
    }

    private void executeSqlToShardingDB(final String sqlString) {
        try (
                Connection ds0Connection = openConnection("demo_ds_0");
                Connection ds1Connection = openConnection("demo_ds_1");
                Connection ds2Connection = openConnection("demo_ds_2")) {
            ds0Connection.createStatement().executeUpdate(sqlString);
            ds1Connection.createStatement().executeUpdate(sqlString);
            ds2Connection.createStatement().executeUpdate(sqlString);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
