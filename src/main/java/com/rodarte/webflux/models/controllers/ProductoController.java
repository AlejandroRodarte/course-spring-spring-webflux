package com.rodarte.webflux.models.controllers;

import com.rodarte.webflux.models.documents.Producto;
import com.rodarte.webflux.models.services.ProductoService;
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
    private ProductoService productoService;

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({ "/", "/listar" })
    public String listar(Model model) {

        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos
            .subscribe(
                producto -> logger.info(producto.getNombre())
            );

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return "listar";

    }

    @GetMapping("/listar-data-driver")
    public String listarDataDriver(Model model) {

        Flux<Producto> productos =
            productoService
                .findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productos
            .subscribe(
                producto -> logger.info(producto.getNombre())
            );

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");

        return "listar";

    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {

        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");

        return "listar";

    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");

        return "listar-chunked";

    }

}
