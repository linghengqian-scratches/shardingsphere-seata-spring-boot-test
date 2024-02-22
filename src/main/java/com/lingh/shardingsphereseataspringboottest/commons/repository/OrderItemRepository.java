package com.lingh.shardingsphereseataspringboottest.commons.repository;

import com.lingh.shardingsphereseataspringboottest.commons.entity.OrderItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "UnusedReturnValue"})
public final class OrderItemRepository {

    private final DataSource dataSource;

    public OrderItemRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * create table if not exists in Postgres.
     *
     * @throws SQLException SQL exception
     */
    public void createTableIfNotExistsInPostgres() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS t_order_item (
                    order_item_id BIGSERIAL PRIMARY KEY,
                    order_id BIGINT NOT NULL,
                    user_id INTEGER NOT NULL,
                    phone VARCHAR(50),
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
        String sql = "DROP TABLE IF EXISTS t_order_item";
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
        String sql = "TRUNCATE TABLE t_order_item";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    /**
     * insert something to table.
     *
     * @param orderItem orderItem
     * @return orderItemId of the insert statement
     * @throws SQLException SQL exception
     */
    public Long insert(final OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO t_order_item (order_id, user_id, phone, status) VALUES (?, ?, ?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderItem.getOrderId());
            preparedStatement.setInt(2, orderItem.getUserId());
            preparedStatement.setString(3, orderItem.getPhone());
            preparedStatement.setString(4, orderItem.getStatus());
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    orderItem.setOrderItemId(resultSet.getLong(1));
                }
            }
        }
        return orderItem.getOrderItemId();
    }

    /**
     * delete by orderItemId.
     *
     * @param orderItemId orderItemId
     * @throws SQLException SQL exception
     */
    public void delete(final Long orderItemId) throws SQLException {
        String sql = "DELETE FROM t_order_item WHERE order_id=?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderItemId);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * select all.
     *
     * @return list of OrderItem
     * @throws SQLException SQL exception
     */
    public List<OrderItem> selectAll() throws SQLException {
        String sql = "SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id";
        List<OrderItem> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(resultSet.getLong(1));
                orderItem.setOrderId(resultSet.getLong(2));
                orderItem.setUserId(resultSet.getInt(3));
                orderItem.setPhone(resultSet.getString(4));
                orderItem.setStatus(resultSet.getString(5));
                result.add(orderItem);
            }
        }
        return result;
    }
}
