package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private String orderNumber;
    private Long cashRegisterId;
    private String orderType;
    private String tableNumber;
    private String status;
    private String notes;
    private List<OrderItem> items = new ArrayList<>();
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long modifiedBy;
    private LocalDateTime modifiedAt;
    private Long deletedBy;
    private LocalDateTime deletedAt;
    private String createdByName;
    private String modifiedByName;
    private String deletedByName;

}