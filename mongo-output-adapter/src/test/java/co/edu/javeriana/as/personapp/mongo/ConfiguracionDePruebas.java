package co.edu.javeriana.as.personapp.mongo;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuración mínima para las pruebas de este módulo. El adaptador es una
 * librería sin clase de arranque propia, y las porciones de prueba de Spring Boot
 * buscan una @SpringBootConfiguration hacia arriba desde la clase de prueba.
 * Solo se usa en pruebas (HU-013).
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "co.edu.javeriana.as.personapp.mongo")
public class ConfiguracionDePruebas {
}
