package com.example.groceryserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.groceryserver.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}
