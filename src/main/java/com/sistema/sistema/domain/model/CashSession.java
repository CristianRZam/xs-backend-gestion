package com.sistema.sistema.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashSession {

    private Long id;
    private Long cashRegisterId;
    private Long openedBy;
    private String openedByName;
    private LocalDateTime openedAt;
    private BigDecimal openingAmount;
    private Long closedBy;
    private String closedByName;
    private LocalDateTime closedAt;
    private BigDecimal expectedAmount;
    private BigDecimal closingAmount;
    private BigDecimal difference;
    private String status;
    private String openingComment;
    private String closingComment;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long modifiedBy;
    private LocalDateTime modifiedAt;
    private Boolean deleted;

}