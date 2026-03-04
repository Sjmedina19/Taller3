import java.io.*;
import java.util.*;

public class ECommerce {

    static final String PRODUCTOS = "productos.csv";
    static final String CLIENTES = "clientes.csv";
    static final String PEDIDOS = "pedidos.csv";
    static final String TOTAL_VENTAS = "total_ventas.csv";

    static class Product {
        int id;
        String nombre;
        String categoria;
        long precio;
        int stock;

        Product(int id, String nombre, String categoria, long precio, int stock) {
            this.id = id; this.nombre = nombre; this.categoria = categoria; this.precio = precio; this.stock = stock;
        }
    }

    static class Client {
        int id; String nombre; String email;
        Client(int id, String nombre, String email) { this.id = id; this.nombre = nombre; this.email = email; }
    }

    static class Order {
        int id; int clienteId; int productoId; int cantidad; String fecha;
        Order(int id, int clienteId, int productoId, int cantidad, String fecha) { this.id=id; this.clienteId=clienteId; this.productoId=productoId; this.cantidad=cantidad; this.fecha=fecha; }
    }

    static class TotalSale {
        int productoId; String nombreProducto; long total;
        TotalSale(int productoId, String nombreProducto, long total) { this.productoId=productoId; this.nombreProducto=nombreProducto; this.total=total; }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Menú E-Commerce ---");
            System.out.println("1. Ver productos ordenados por precio");
            System.out.println("2. Agregar un nuevo cliente");
            System.out.println("3. Calcular el total de ventas por producto");
            System.out.println("4. Ver clientes que han realizado compras");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1": opcion1(); break;
                case "2": opcion2(sc); break;
                case "3": opcion3(); break;
                case "4": opcion4(); break;
                case "5": System.out.println("Saliendo..."); return;
                default: System.out.println("Opción inválida");
            }
        }
    }

    // Opción 1: Ordenar productos por precio (menor a mayor) y actualizar productos.csv
    static void opcion1() {
        List<Product> products = readProducts();
        if (products.isEmpty()) { System.out.println("No hay productos."); return; }
        // Implementación de ordenamiento: selection sort por precio ascendente
        for (int i = 0; i < products.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < products.size(); j++) {
                if (products.get(j).precio < products.get(minIdx).precio) minIdx = j;
            }
            if (minIdx != i) {
                Product tmp = products.get(i);
                products.set(i, products.get(minIdx));
                products.set(minIdx, tmp);
            }
        }
        // Mostrar
        System.out.println("\nProductos ordenados por precio (menor -> mayor):");
        System.out.printf("% -5s %-30s %-12s %-6s\n","ID","Nombre","Precio","Stock");
        for (Product p : products) {
            System.out.printf("% -5d %-30s %-12d %-6d\n", p.id, p.nombre, p.precio, p.stock);
        }
        // Escribir de vuelta en el CSV
        writeProducts(products);
        System.out.println("Archivo 'productos.csv' actualizado.");
    }

    // Opción 2: Agregar nuevo cliente con id autoincrementado
    static void opcion2(Scanner sc) {
        List<Client> clients = readClients();
        System.out.print("Nombre: "); String nombre = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        int nextId = 1;
        for (Client c : clients) if (c.id >= nextId) nextId = c.id + 1;
        // Añadir al archivo (append)
        try (FileWriter fw = new FileWriter(CLIENTES, true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.format("%d,%s,%s\n", nextId, nombre, email));
            System.out.println("Cliente agregado con id: " + nextId);
        } catch (IOException e) {
            System.out.println("Error al escribir en clientes.csv: " + e.getMessage());
        }
    }

    // Opción 3: Calcular total de ventas por producto y guardar en total_ventas.csv
    static void opcion3() {
        List<Product> products = readProducts();
        List<Order> orders = readOrders();
        if (products.isEmpty()) { System.out.println("No hay productos."); return; }
        // Map id -> Product
        Map<Integer, Product> map = new HashMap<>();
        for (Product p : products) map.put(p.id, p);
        Map<Integer, Long> totals = new HashMap<>();
        for (Order o : orders) {
            Product p = map.get(o.productoId);
            if (p != null) {
                long add = p.precio * (long)o.cantidad;
                totals.put(p.id, totals.getOrDefault(p.id, 0L) + add);
            }
        }
        List<TotalSale> list = new ArrayList<>();
        for (Product p : products) {
            long t = totals.getOrDefault(p.id, 0L);
            list.add(new TotalSale(p.id, p.nombre, t));
        }
        // Ordenar por total descendente (selection sort)
        for (int i = 0; i < list.size() - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).total > list.get(maxIdx).total) maxIdx = j;
            }
            if (maxIdx != i) {
                TotalSale tmp = list.get(i);
                list.set(i, list.get(maxIdx));
                list.set(maxIdx, tmp);
            }
        }
        // Mostrar
        System.out.println("\nTotal de ventas por producto (mayor -> menor):");
        System.out.printf("% -5s %-30s %-12s\n","ID","Nombre","Total");
        for (TotalSale t : list) {
            System.out.printf("% -5d %-30s %-12d\n", t.productoId, t.nombreProducto, t.total);
        }
        // Escribir en total_ventas.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOTAL_VENTAS))) {
            pw.println("producto_id,nombre_producto,total");
            for (TotalSale t : list) pw.println(String.format("%d,%s,%d", t.productoId, t.nombreProducto, t.total));
            System.out.println("Archivo 'total_ventas.csv' escrito.");
        } catch (IOException e) {
            System.out.println("Error al escribir total_ventas.csv: " + e.getMessage());
        }
    }

    // Opción 4: Ver clientes que han realizado compras (ordenados alfabéticamente)
    static void opcion4() {
        List<Client> clients = readClients();
        List<Order> orders = readOrders();
        Set<Integer> compradores = new HashSet<>();
        for (Order o : orders) compradores.add(o.clienteId);
        List<Client> result = new ArrayList<>();
        for (Client c : clients) if (compradores.contains(c.id)) result.add(c);
        if (result.isEmpty()) { System.out.println("No hay clientes que hayan realizado compras."); return; }
        // Ordenar por nombre asc (selection sort)
        for (int i = 0; i < result.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < result.size(); j++) {
                if (result.get(j).nombre.compareToIgnoreCase(result.get(minIdx).nombre) < 0) minIdx = j;
            }
            if (minIdx != i) {
                Client tmp = result.get(i);
                result.set(i, result.get(minIdx));
                result.set(minIdx, tmp);
            }
        }
        System.out.println("\nClientes que han realizado compras (ordenados):");
        System.out.printf("% -5s %-20s %-30s\n","ID","Nombre","Email");
        for (Client c : result) System.out.printf("% -5d %-20s %-30s\n", c.id, c.nombre, c.email);
    }

    // Lectura/escritura de archivos
    static List<Product> readProducts() {
        List<Product> list = new ArrayList<>();
        File f = new File(PRODUCTOS);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // cabecera
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String nombre = parts[1];
                String categoria = parts[2];
                long precio = Long.parseLong(parts[3]);
                int stock = Integer.parseInt(parts[4]);
                list.add(new Product(id, nombre, categoria, precio, stock));
            }
        } catch (IOException e) { System.out.println("Error leyendo productos: " + e.getMessage()); }
        return list;
    }

    static void writeProducts(List<Product> products) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTOS))) {
            pw.println("id,nombre,categoria,precio,stock");
            for (Product p : products) pw.println(String.format("%d,%s,%s,%d,%d", p.id, p.nombre, p.categoria, p.precio, p.stock));
        } catch (IOException e) { System.out.println("Error escribiendo productos: " + e.getMessage()); }
    }

    static List<Client> readClients() {
        List<Client> list = new ArrayList<>();
        File f = new File(CLIENTES);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // cabecera
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String nombre = parts[1];
                String email = parts[2];
                list.add(new Client(id, nombre, email));
            }
        } catch (IOException e) { System.out.println("Error leyendo clientes: " + e.getMessage()); }
        return list;
    }

    static List<Order> readOrders() {
        List<Order> list = new ArrayList<>();
        File f = new File(PEDIDOS);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // cabecera
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                int clienteId = Integer.parseInt(parts[1]);
                int productoId = Integer.parseInt(parts[2]);
                int cantidad = Integer.parseInt(parts[3]);
                String fecha = parts[4];
                list.add(new Order(id, clienteId, productoId, cantidad, fecha));
            }
        } catch (IOException e) { System.out.println("Error leyendo pedidos: " + e.getMessage()); }
        return list;
    }
}
