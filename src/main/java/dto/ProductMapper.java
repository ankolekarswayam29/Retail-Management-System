
package com.retailpro.retailmanagementsystem.dto;

import com.retailpro.retailmanagementsystem.dto.ProductRequest;
import com.retailpro.retailmanagementsystem.dto.ProductResponse;
import com.retailpro.retailmanagementsystem.entity.Category;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Supplier;

public class ProductMapper {

    // Convert ProductRequest into Product entity
    public static Product toEntity(ProductRequest request) {

        Product product = new Product();

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = new Supplier();
            supplier.setId(request.getSupplierId());
            product.setSupplier(supplier);
        }

        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinimumStock(request.getMinimumStock());
        product.setUnit(request.getUnit());
        product.setImagePath(request.getImagePath());
        product.setActive(request.getActive());

        return product;
    }

    // Convert Product entity into ProductResponse
    public static ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setProductCode(product.getProductCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getSupplier() != null) {
            response.setSupplierId(product.getSupplier().getId());
            response.setSupplierName(product.getSupplier().getName());
        }

        response.setPurchasePrice(product.getPurchasePrice());
        response.setSellingPrice(product.getSellingPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setMinimumStock(product.getMinimumStock());
        response.setUnit(product.getUnit());
        response.setImagePath(product.getImagePath());
        response.setActive(product.getActive());

        response.setLowStock(product.isLowStock());
        response.setOutOfStock(product.isOutOfStock());

        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
}
