package com.rodarte.webflux.models.controllers;

import com.rodarte.webflux.models.dao.ProductoDao;
import com.rodarte.webflux.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoDao productoDao;

    private static final Logger logger = LoggerFactory.getLogger(ProductoRestController.class);

    @GetMapping
    public Flux<Producto> index() {

        return productoDao
                .findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                })
                .doOnNext(producto -> logger.info(producto.getNombre()));

    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id) {
        return productoDao
                .findAll()
                .filter(producto -> producto.getId().equals(id))
                .next()
                .doOnNext(producto -> logger.info(producto.getNombre()));
        // return productoDao.findById(id);
    }

}
