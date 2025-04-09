package com.main.config;

import com.main.dtos.OrderItemDto;
import com.main.dtos.OrderItemRequestDto;
import com.main.dtos.OrderRequest;
import com.main.dtos.OrderResponse;
import com.main.entity.Order;
import com.main.entity.OrderItem;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OrderConfig {
    private final ModelMapper modelMapper;

    public OrderConfig(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureModelMapper();
    }

    private void configureModelMapper() {
        // Map OrderRequest to Order
        modelMapper.typeMap(OrderRequest.class, Order.class)
                .addMappings(mapper -> mapper.map(
                        OrderRequest::getItems,
                        Order::setItems
                ));

        // Map OrderItemRequestDto to OrderItem
        modelMapper.typeMap(OrderItemRequestDto.class, OrderItem.class)
                .addMappings(mapper -> {
                    mapper.map(OrderItemRequestDto::getProductId, OrderItem::setProductId);
                    mapper.map(OrderItemRequestDto::getQuantity, OrderItem::setQuantity);
                });

        // Map Order to OrderResponse
        modelMapper.typeMap(Order.class, OrderResponse.class)
                .addMappings(mapper -> mapper.map(
                        order -> convertItemListToDtoList(order.getItems()),
                        OrderResponse::setItems
                ));
    }

    private List<OrderItem> convertDtoListToItemList(List<OrderItemRequestDto> itemDtoList) {
        if (itemDtoList == null) return List.of();
        return itemDtoList.stream()
                .map(dto -> modelMapper.map(dto, OrderItem.class))
                .toList();
    }

    private List<OrderItemDto> convertItemListToDtoList(List<OrderItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(item -> modelMapper.map(item, OrderItemDto.class))
                .toList();
    }
}