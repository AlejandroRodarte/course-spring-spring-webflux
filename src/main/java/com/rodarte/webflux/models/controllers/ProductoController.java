package com.rodarte.webflux.models.controllers;

import com.rodarte.webflux.models.dao.ProductoDao;
import com.rodarte.webflux.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
public class ProductoController {

    @Autowired
    private ProductoDao productoDao;

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({ "/", "/listar" })
    private String listar(Model model) {

        Flux<Producto> productos =
            productoDao
                .findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                });

        productos
            .subscribe(
                producto -> logger.info(producto.getNombre())
            );

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return "listar";

    }

    @GetMapping("/listar-data-driver")
    private String listarDataDriver(Model model) {

        Flux<Producto> productos =
            productoDao
                    .findAll()
                    .map(producto -> {
                        producto.setNombre(producto.getNombre().toUpperCase());
                        return producto;
                    })
                    .delayElements(Duration.ofSeconds(1));

        productos
            .subscribe(
                producto -> logger.info(producto.getNombre())
            );

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");

        return "listar";

    }

}
