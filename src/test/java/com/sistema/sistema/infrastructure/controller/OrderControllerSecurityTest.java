package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.order.OrderItemStatusRequest;
import com.sistema.sistema.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderControllerSecurityTest {

    @Test
    void itemEndpointsDeclareTheirRequiredAuthorities() throws NoSuchMethodException {
        assertAuthority("addItem", "ADD_ORDER_ITEM", Long.class, OrderItem.class);
        assertAuthority("updatePendingItem", "EDIT_PENDING_ORDER_ITEM", Long.class, Long.class, OrderItem.class);
        assertAuthority("cancelPendingItem", "CANCEL_ORDER_ITEM", Long.class, Long.class, String.class);
        assertAuthority("getItemStatusHistory", "VIEW_ORDER_ITEM_HISTORY", Long.class, Long.class);
    }

    @Test
    void statusEndpointRequiresTheAuthorityForEachAllowedTransition() throws NoSuchMethodException {
        Method method = OrderController.class.getDeclaredMethod(
                "updateItemStatus", Long.class, Long.class, OrderItemStatusRequest.class
        );
        PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);

        assertNotNull(authorization);
        assertTrue(authorization.value().contains("PREPARE_ORDER_ITEM"));
        assertTrue(authorization.value().contains("MARK_ORDER_ITEM_READY"));
        assertTrue(authorization.value().contains("SERVE_ORDER_ITEM"));
    }

    private void assertAuthority(String methodName, String authority, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = OrderController.class.getDeclaredMethod(methodName, parameterTypes);
        PreAuthorize authorization = method.getAnnotation(PreAuthorize.class);
        assertNotNull(authorization);
        assertTrue(authorization.value().contains(authority));
    }
}
