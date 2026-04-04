package com.cyboul.cloudplatform.common.dto;

public record ProductDTO(
        String id,
        String name,
        double price,
        int stock
) {}
