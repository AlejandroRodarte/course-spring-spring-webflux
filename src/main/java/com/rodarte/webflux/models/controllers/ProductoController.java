package com.rodarte.webflux.models.controllers;

import com.rodarte.webflux.models.documents.Categoria;
import com.rodarte.webflux.models.documents.Producto;
import com.rodarte.webflux.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;
import java.util.Date;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categorias")
    public Flux<Categoria> categorias() {
        return productoService.findAllCategoria();
    }

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
    public Mono<String> guardar(
        @Valid @ModelAttribute("producto") Producto producto,
        BindingResult bindingResult,
        Model model,
        SessionStatus sessionStatus
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("titulo", "Errores en el formulario producto");
            model.addAttribute("boton", "Guardar");

            return Mono.just("form");

        }

        sessionStatus.setComplete();

        if (producto.getCreatedAt() == null) {
            producto.setCreatedAt(new Date());
        }

        // thenReturn: permite retornar un Mono cuando un Observable completa; permitira redirigir cuando
        // el producto haya sido salvado
        return productoService
                .save(producto)
                .doOnNext(p -> logger.info("Producto guardado: " + p.getNombre() + " Id: " + p.getId()))
                .thenReturn("redirect:/listar?success=producto+guardado+con+exito");

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

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {

        return
            productoService
                .findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {

                    if (producto.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }

                    return Mono.just(producto);

                })
                .doOnNext(producto -> {
                    logger.info("Eliminando producto: " + producto.getNombre());
                    logger.info("Eliminando producto Id: " + producto.getId());
                })
                .flatMap(producto -> productoService.delete(producto))
                .thenReturn("redirect:/listar?success=producto+eliminado+con+exito")
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));

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
