package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.Order;

import java.util.List;

public interface OrderRepository {

    Order create(Order order);

    Order getById(Long id);

    List<Order> getAll();

    Order update(Long id, Order order);

    Order updateStatus(Long id, String status);

    void delete(Long id);

}