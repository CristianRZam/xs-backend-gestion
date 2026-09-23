package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.domain.model.Order;

import java.util.List;
import java.time.LocalDate;
import com.sistema.sistema.application.dto.response.PageResponseDTO;

public interface OrderUseCase {

    Order create(Order order);

    Order getById(Long id);

    List<Order> getAll();
    PageResponseDTO<Order> getPage(int page, int size, LocalDate fromDate, LocalDate toDate);

    Order update(Long id, Order order);

    Order updateStatus(Long id, String status);

    void delete(Long id);

}
