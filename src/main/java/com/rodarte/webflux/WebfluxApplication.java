package com.rodarte.webflux;

import com.rodarte.webflux.models.documents.Categoria;
import com.rodarte.webflux.models.documents.Producto;
import com.rodarte.webflux.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class WebfluxApplication implements CommandLineRunner {

	@Autowired
	ProductoService productoService;

	@Autowired
	ReactiveMongoTemplate reactiveMongoTemplate;

	private static final Logger logger = LoggerFactory.getLogger(WebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		reactiveMongoTemplate
			.dropCollection("productos")
			.subscribe();

		reactiveMongoTemplate
			.dropCollection("categorias")
			.subscribe();

		Categoria electronico = new Categoria("Electronico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computacion");
		Categoria muebles = new Categoria("Muebles");

		Flux
			.just(
				electronico,
				deporte,
				computacion,
				muebles
			)
			.flatMap(productoService::saveCategoria)
			.doOnNext(categoria -> logger.info("Categoria creada: " + categoria.getNombre() + ", Id: " + categoria.getId()))
			.thenMany(
				Flux
					.just(
						new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
						new Producto("Sony Camara HD Digital", 177.89, electronico),
						new Producto("Apple iPod", 46.89, electronico),
						new Producto("Sony Notebook", 846.89, computacion),
						new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
						new Producto("Bianchi Bicicleta", 70.89, deporte),
						new Producto("HP Notebook Omen 17", 2500.89, computacion),
						new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
						new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
					)
					.flatMap(producto -> {
						producto.setCreatedAt(new Date());
						return productoService.save(producto);
					})
			)
			.subscribe(
				producto -> logger.info("Insert: " + producto.getId() + " " + producto.getNombre())
			);


	}

}
