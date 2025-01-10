package es.daw.primercrud.controller;

import es.daw.primercrud.dto.ProductoDTO;
import es.daw.primercrud.entity.Producto;
import es.daw.primercrud.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import es.daw.primercrud.repository.ProductoRepository;

/*
 * Puedes definir un mapeo base en la clase con @RequestMapping y luego usar anotaciones específicas como @GetMapping, @PostMapping, etc., en cada método.
 * Esto no es solo válido, sino una práctica común para agrupar rutas relacionadas bajo un mismo prefijo
 * GET /productos
 * POST /productos
 * PUT /productos/{id}
 * DELETE /productos/{id}
 */
@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoService productoService;

    // -----------------------------------
    /**
     * Devolver todos los productos
     * @return Json con objetos productos
     */
    @GetMapping
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    // ----------------------------------
    @GetMapping("/list-dto")
    public List<ProductoDTO> findAllDTO(){
        List<Producto> productos = productoRepository.findAll();


//        return productos.stream()
//                .map(p -> new ProductoDTO(p.getNombre(),p.getSku(),p.getPrecio()))
//                .collect(Collectors.toList());

//        return productos.stream()
//                .map(p -> new ProductoDTO(p.getNombre(),p.getSku(),p.getPrecio()))
//                .toList();

        return productos.stream()
                .map(p -> productoService.convert2ProductoDTO(p).get())
                .toList();

//        ArrayList<ProductoDTO> productoDTOs = new ArrayList<>();
//        for (Producto p : productos) {
//            productoDTOs.add(productoService.convert2ProductoDTO(p).get());
//        }
//        return productoDTOs;


    }

    // -----------------------------------

    // -----------------------------------
    // @RequestBody Recibe los datos en el cuerpo de la solicitud HTTP (body)
    // Los datos deben estar en formato JSON
    // El método con @RequestBody es preferible para aplicaciones REST porque es más limpio, flexible y estándar
    // La práctica más común es enviar los datos en el cuerpo de la solicitud en lugar de la URL.
    // Así se aprovechan estándares como JSON o XML para transferir estructuras de datos más complejas.
    /*
        {
          "nombre": "Laptop",
          "precio": 1500,
          "sku": "LPT123"
        }
     */
    @PostMapping
    public Producto add(@RequestBody Producto producto) {
        return productoRepository.save(producto);
    }

    // ----------------------------------
    // PENDIENTE: añadir un nuevo endpoint que reciba un ProductoDTO
    @PostMapping("/add-dto")
    public ResponseEntity<Void> add(@RequestBody ProductoDTO productoDTO){
        Optional<Producto> optProducto = productoService.convert2Producto(productoDTO);
        if(optProducto.isPresent()) {
            Producto producto = optProducto.get();
            productoRepository.save(producto);
            //return ResponseEntity.ok().build();
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        // 400
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.badRequest().build();

    }

    @PostMapping("/add-dto2")
    public ResponseEntity<ProductoDTO> add2(@RequestBody ProductoDTO productoDTO){
        Optional<Producto> optProducto = productoService.convert2Producto(productoDTO);
        if(optProducto.isPresent()) {
            productoRepository.save(optProducto.get());
            //return ResponseEntity.ok(productoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoDTO);
        }
        return ResponseEntity.badRequest().build();
    }

    // -----------------------------------
    @PostMapping("/add")
    public Producto add(@RequestParam String nombre, @RequestParam int precio, @RequestParam String sku) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setSku(sku);
        return productoRepository.save(producto);
    }
    // -----------------------------------
    @PostMapping("/add2")
    public String add2(@RequestParam String nombre, @RequestParam int precio, @RequestParam String sku) {
         Producto producto = new Producto();
         producto.setNombre(nombre);
         producto.setPrecio(precio);
         producto.setSku(sku);

         productoRepository.save(producto);
         return "Added new product to repo!";
    }

    @PostMapping("/add-nativo")
    public String add(HttpServletRequest request) {
        // Sería más óptimo usar el entity Producto
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setNombre(request.getParameter("nombre"));
        productoDTO.setSku(request.getParameter("sku"));
        productoDTO.setPrecio(Integer.parseInt(request.getParameter("precio")));

        productoService.convert2Producto(productoDTO)
                .ifPresent(producto -> productoRepository.save(producto));

        return "Added new product to repo!";

    }

    // -----------------------------------
    @GetMapping("/{id}")
    public Producto findById(@PathVariable BigInteger id) {
        //return productoRepository.findById(id).get();
        return productoRepository.findById(id).orElse(null);
    }


    // -----------------------------------
    /*
     * La clase ResponseEntity en Spring Framework se usa para personalizar las respuestas HTTP de un controlador REST.
     * Permite establecer tanto el cuerpo como los encabezados y el estado HTTP de la respuesta.
     * ResponseEntity.noContent().build() ==> Devuelve una respuesta HTTP con el estado 204 No Content. Exitosa pero sin contenido.
     * ResponseEntity.notFound().build() ==> Devuelve una respuesta HTTP con el estado 404 Not Found. Recurso solicitado no existe.
     * ResponseEntity.ok(producto) ==> Devuelve una respuesta HTTP con el estado 200 OK. Incluye un cuerpo con el objeto, que se serializa automáticamente a JSON
     * ResponseEntity.ok().build(); ==> evuelve una respuesta HTTP con el estado 200 OK (texto, no incluye el json del objeto)
     */

    // El recurso no existe y es un error del cliente ==> 404 (not found)
    // La ausencia del recurso es esperada o normal ==> 204 (no content)
    // API sigue estándares REST ==> 404 más habitual
    // Operaciones asincrónicas o procesos => 204

    @GetMapping("/find/{id}")
    public ResponseEntity<Producto> findById2(@PathVariable BigInteger id) {

//        Optional<Producto> optProd = productoRepository.findById(id);
//        if (optProd.isPresent()) {
//            return ResponseEntity.ok(optProd.get());
//        }
//        return ResponseEntity.noContent().build(); // 204

        // Si se encuentra un producto con el id dado, el Optional contendrá ese producto.
        // Si no se encuentra un producto, el Optional estará vacío.
        // El método .map() se utiliza para transformar el contenido del Optional si está presente.
        // Si el Optional está vacío (es decir, el producto no existe), el .map() no hará nada y el flujo pasará directamente al método .orElse()
        // El método .orElse() se ejecuta cuando el Optional está vacío.
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    // -----------------------------------
    /*
     * Si defines un método abstracto en una interfaz que extiende JpaRepository,
     * Spring Data JPA generará automáticamente su implementación, siempre y cuando sigas las convenciones de nomenclatura de Spring.
     * Producto findProductoById(Long id);
     *
     */
    @GetMapping("/find-producto/{id}")
    public Producto findProductoById(@PathVariable Long id) {
        return productoRepository.findProductoById(id);
    }

    // -----------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Producto> delete(@PathVariable BigInteger id) {

//        if(productoRepository.existsById(id)) {
//            productoRepository.deleteById(id);
//            return ResponseEntity.ok().build();
//        }
//        return ResponseEntity.notFound().build(); // 404

        return productoRepository.findById(id)
                .map(producto -> {
                    productoRepository.deleteById(id); // Borra el producto
                    return ResponseEntity.ok(producto); // Devuelve el producto eliminado
                })
                .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404
    }

    @PutMapping("/{id}")
    //Pasamos un json del producto!!!! no por requestParam
    public ResponseEntity<Producto> update(@PathVariable BigInteger id, @RequestBody Producto detalles) {

        // FORMA 1: IMPERATIVA
//        Optional<Producto> optProducto =  productoRepository.findById(id);
//        if (optProducto.isPresent()) {
//            Producto producto = optProducto.get();
//            // Para ampliar que actualice por todas las propiedades del json de entrada
//            // debo comprar que no sea la propiedad null o cadena vacía...
//            producto.setNombre(detalles.getNombre());
//            producto.setPrecio(detalles.getPrecio());
//            productoRepository.save(producto);
//            //return ResponseEntity.ok().build();
//            return ResponseEntity.ok(producto);
//        }else{
//            return ResponseEntity.notFound().build(); // status http 404
//        }

        // FORMA 2: DECLARATIVA/FUNCIONAL
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setNombre(detalles.getNombre());
                    producto.setPrecio(detalles.getPrecio());
                    productoRepository.save(producto);
                    return ResponseEntity.ok(producto);
                }).orElse(ResponseEntity.notFound().build()); //si el optional está vacío devuelve un 404

    }

}