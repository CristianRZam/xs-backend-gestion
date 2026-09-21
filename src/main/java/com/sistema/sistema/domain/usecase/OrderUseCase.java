package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.domain.model.Order;

import java.util.List;

public interface OrderUseCase {

    Order create(Order order);

    Order getById(Long id);

    List<Order> getAll();

    Order update(Long id, Order order);

    Order updateStatus(Long id, String status);

    void delete(Long id);

}