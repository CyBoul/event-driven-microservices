package com.cyboul.cloudplatform.commonlib.dto;

public record ProductDTO(
        String id,
        String name,
        double price,
        int stock
) {}
