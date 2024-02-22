package com.lingh.shardingsphereseataspringboottest.commons.repository;

import com.lingh.shardingsphereseataspringboottest.commons.entity.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "SameParameterValue", "UnusedReturnValue"})
public final class OrderRepository {

    private final DataSource dataSource;

    public OrderRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * create table if not exists in Postgres.
     *
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInPostgres() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS t_order (
                    order_id BIGSERIAL PRIMARY KEY,
                    order_type INTEGER,
                    user_id INTEGER NOT NULL,
                    address_id BIGINT NOT NULL,
                    status VARCHAR(50)
                );""";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * drop table.
     *
     * @throws SQLException SQL exception
     */
    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_order";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * truncate table.
     *
     * @throws SQLException SQL exception
     */
    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_order";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * insert Order to table.
     *
     * @param order order
     * @return orderId of the insert statement
     * @throws SQLException SQL Exception
     */
    public Long insert(final Order order) throws SQLException {
        String sql = "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setInt(2, order.getOrderType());
            preparedStatement.setLong(3, order.getAddressId());
            preparedStatement.setString(4, order.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getLong(1));
                }
            }
        }
        return order.getOrderId();
    }

    /**
     * delete by orderId.
     *
     * @param orderId orderId
     * @throws SQLException SQL exception
     */
    public void delete(final Long orderId) throws SQLException {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * select all.
     *
     * @return list of Order
     * @throws SQLException SQL exception
     */
    public List<Order> selectAll() throws SQLException {
        return getOrders("SELECT * FROM t_order");
    }

    /**
     * get Orders by SQL.
     *
     * @param sql SQL
     * @return list of Order
     * @throws SQLException SQL exception
     */
    private List<Order> getOrders(final String sql) throws SQLException {
        List<Order> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getLong(1));
                order.setOrderType(resultSet.getInt(2));
                order.setUserId(resultSet.getInt(3));
                order.setAddressId(resultSet.getLong(4));
                order.setStatus(resultSet.getString(5));
                result.add(order);
            }
        }
        return result;
    }
}
