package com.rodarte.webflux.models.dao;

import com.rodarte.webflux.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {
}
