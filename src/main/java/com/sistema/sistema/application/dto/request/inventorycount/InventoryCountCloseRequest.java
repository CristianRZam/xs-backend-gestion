package com.sistema.sistema.application.dto.request.inventorycount;
import lombok.*; import java.util.*;
@Getter @Setter public class InventoryCountCloseRequest { private String closingComment; private List<Item> items; @Getter @Setter public static class Item { private Long productId; private Long physicalStock; private String reason; private String comment; private Boolean applyAdjustment; } }
