package com.lingh.shardingsphereseataspringboottest.commons;

import com.lingh.shardingsphereseataspringboottest.commons.entity.Address;
import com.lingh.shardingsphereseataspringboottest.commons.entity.Order;
import com.lingh.shardingsphereseataspringboottest.commons.entity.OrderItem;
import com.lingh.shardingsphereseataspringboottest.commons.repository.AddressRepository;
import com.lingh.shardingsphereseataspringboottest.commons.repository.OrderItemRepository;
import com.lingh.shardingsphereseataspringboottest.commons.repository.OrderRepository;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Getter
public final class TestShardingService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final AddressRepository addressRepository;

    public TestShardingService(final DataSource dataSource) {
        orderRepository = new OrderRepository(dataSource);
        orderItemRepository = new OrderItemRepository(dataSource);
        addressRepository = new AddressRepository(dataSource);
    }

    /**
     * Process success.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void processSuccess() throws SQLException {
        final Collection<Long> orderIds = insertData();
        Collection<Order> orders = orderRepository.selectAll();
        assertThat(orders.stream().map(Order::getOrderType).collect(Collectors.toList()),
                equalTo(Arrays.asList(1, 1, 1, 1, 1, 0, 0, 0, 0, 0)));
        assertThat(orders.stream().map(Order::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 2, 4, 6, 8, 10))));
        assertThat(orders.stream().map(Order::getAddressId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1L, 3L, 5L, 7L, 9L, 2L, 4L, 6L, 8L, 10L))));
        assertThat(orders.stream().map(Order::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        Collection<OrderItem> orderItems = orderItemRepository.selectAll();
        assertThat(orderItems.stream().map(OrderItem::getUserId).collect(Collectors.toList()),
                equalTo(new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 2, 4, 6, 8, 10))));
        assertThat(orderItems.stream().map(OrderItem::getPhone).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "13800000001").collect(Collectors.toList())));
        assertThat(orderItems.stream().map(OrderItem::getStatus).collect(Collectors.toList()),
                equalTo(IntStream.range(1, 11).mapToObj(i -> "INSERT_TEST").collect(Collectors.toList())));
        assertThat(addressRepository.selectAll(),
                equalTo(LongStream.range(1, 11).mapToObj(i -> new Address(i, "address_test_" + i)).collect(Collectors.toList())));
        deleteData(orderIds);
        assertThat(orderRepository.selectAll(), equalTo(new ArrayList<>()));
        assertThat(orderItemRepository.selectAll(), equalTo(new ArrayList<>()));
        assertThat(addressRepository.selectAll(), equalTo(new ArrayList<>()));
        addressRepository.assertRollbackWithTransactions();
    }

    /**
     * Insert data.
     *
     * @return orderId of the insert statement.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public Collection<Long> insertData() throws SQLException {
        Collection<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setOrderType(i % 2);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setUserId(i);
            orderItem.setPhone("13800000001");
            orderItem.setStatus("INSERT_TEST");
            orderItemRepository.insert(orderItem);
            Address address = new Address((long) i, "address_test_" + i);
            addressRepository.insert(address);
            result.add(order.getOrderId());
        }
        return result;
    }

    /**
     * Delete data.
     *
     * @param orderIds orderId of the insert statement.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void deleteData(final Collection<Long> orderIds) throws SQLException {
        long count = 1;
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
            addressRepository.delete(count++);
        }
    }

    /**
     * Clean environment.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderItemRepository.dropTable();
        addressRepository.dropTable();
    }
}
