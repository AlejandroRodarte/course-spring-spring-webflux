package com.rodarte.webflux.models.controllers;

import com.rodarte.webflux.models.documents.Producto;
import com.rodarte.webflux.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({ "/", "/listar" })
    public Mono<String> listar(Model model) {

        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos
            .subscribe(
                producto -> logger.info(producto.getNombre())
            );

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");

        return Mono.just("listar");

    }

    // podemos retornar los strings de las vistas html de forma reactiva con Mono
    @GetMapping("/form")
    public Mono<String> crear(Model model) {

        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        model.addAttribute("boton", "Crear");

        return Mono.just("form");

    }

    @PostMapping("/form")
    public Mono<String> guardar(Producto producto, SessionStatus sessionStatus) {

        sessionStatus.setComplete();

        // thenReturn: permite retornar un Mono cuando un Observable completa; permitira redirigir cuando
        // el producto haya sido salvado
        return productoService
                .save(producto)
                .doOnNext(p -> logger.info("Producto guardado: " + p.getNombre() + " Id: " + p.getId()))
                .thenReturn("redirect:/listar");

    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {

        Mono<Producto> producto =
            productoService
                .findById(id)
                .doOnNext(p -> logger.info("Producto: " + p.getNombre()))
                .defaultIfEmpty(new Producto());

        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", producto);
        model.addAttribute("boton", "Editar");

        return Mono.just("form");

    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable String id, Model model) {

        return
            productoService
                .findById(id)
                .doOnNext(producto -> {

                    logger.info("Producto: " + producto.getNombre());

                    model.addAttribute("titulo", "Editar Producto");
                    model.addAttribute("producto", producto);
                    model.addAttribute("boton", "Editar");

                })
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {

                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }

                    return Mono.just(producto);

                })
                .thenReturn("form")
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));

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
