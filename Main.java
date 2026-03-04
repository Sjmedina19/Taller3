import java.io.*;
import java.util.*;

public class Main {
    private static final String PRODUCTOS_FILE = "productos.csv";
    private static final String CLIENTES_FILE = "clientes.csv";
    private static final String PEDIDOS_FILE = "pedidos.csv";
    private static final String TOTAL_VENTAS_FILE = "total_ventas.csv";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            mostrarMenu();
            System.out.print("Selecciona una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    ordenarProductosPorPrecio();
                    break;
                case 2:
                    agregarNuevoCliente(scanner);
                    break;
                case 3:
                    calcularTotalVentas();
                    break;
                case 4:
                    verClientesConCompras();
                    break;
                case 5:
                    System.out.println("¡Hasta luego!");
                    break;
                default:
                    System.out.println("Opción inválida. Intenta de nuevo.");
            }
        } while (opcion != 5);

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\nMENÚ E-COMMERCE ---");
        System.out.println("1. Ver productos ordenados por precio");
        System.out.println("2. Agregar un nuevo cliente");
        System.out.println("3. Calcular total de ventas por producto");
        System.out.println("4. Ver clientes que han realizado compras");
        System.out.println("5. Salir");
    }

    private static void ordenarProductosPorPrecio() {
        List<Producto> productos = leerProductos();
        ordenarQuickSort(productos, 0, productos.size() - 1);

        System.out.println("\nPRODUCTOS ORDENADOS POR PRECIO ");
        System.out.printf("%-10s %-20s %-15s %-10s%n", "ID", "Nombre", "Precio", "Stock");
        System.out.println("---------------------------------------------");
        for (Producto p : productos) {
            System.out.printf("%-10d %-20s $%-14d %-10d%n", p.id, p.nombre, p.precio, p.stock);
        }
    }

    private static void agregarNuevoCliente(Scanner scanner) {
        List<Cliente> clientes = leerClientes();
        int nuevoId = clientes.isEmpty() ? 1 : clientes.get(clientes.size() - 1).id + 1;

        System.out.print("Ingresa el nombre del cliente: ");
        String nombre = scanner.nextLine();
        System.out.print("Ingresa el email del cliente: ");
        String email = scanner.nextLine();

        Cliente nuevoCliente = new Cliente(nuevoId, nombre, email);
        clientes.add(nuevoCliente);
        guardarClientes(clientes);

        System.out.println("✓ Cliente agregado exitosamente con ID: " + nuevoId);
    }

    private static void calcularTotalVentas() {
        List<Producto> productos = leerProductos();
        List<Pedido> pedidos = leerPedidos();
        List<VentaTotal> ventasTotales = new ArrayList<>();

        for (Producto p : productos) {
            double total = 0;
            for (Pedido ped : pedidos) {
                if (ped.producto_id == p.id) {
                    total += ped.cantidad * p.precio;
                }
            }
            if (total > 0) {
                ventasTotales.add(new VentaTotal(p.id, p.nombre, total));
            }
        }

        ordenarVentasQuickSort(ventasTotales, 0, ventasTotales.size() - 1);
        guardarTotalVentas(ventasTotales);

        System.out.println("\n--- TOTAL DE VENTAS POR PRODUCTO ---");
        System.out.printf("%-10s %-20s %-15s%n", "Producto ID", "Nombre", "Total Ventas");
        System.out.println("---------------------------------------------");
        for (VentaTotal v : ventasTotales) {
            System.out.printf("%-10d %-20s $%-14.0f%n", v.producto_id, v.nombre_producto, v.total);
        }
        System.out.println("✓ Datos guardados en " + TOTAL_VENTAS_FILE);
    }

    private static void verClientesConCompras() {
        List<Cliente> clientes = leerClientes();
        List<Pedido> pedidos = leerPedidos();
        List<String> clientesConCompras = new ArrayList<>();

        for (Pedido ped : pedidos) {
            for (Cliente c : clientes) {
                if (c.id == ped.cliente_id) {
                    if (!clientesConCompras.contains(c.nombre)) {
                        clientesConCompras.add(c.nombre);
                    }
                    break;
                }
            }
        }

        ordenarStringQuickSort(clientesConCompras, 0, clientesConCompras.size() - 1);

        System.out.println("\n--- CLIENTES CON COMPRAS (Ordenados Alfabéticamente) ---");
        for (String nombre : clientesConCompras) {
            System.out.println("• " + nombre);
        }
    }

    private static List<Producto> leerProductos() {
        List<Producto> productos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCTOS_FILE))) {
            String linea = br.readLine();
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                productos.add(new Producto(
                        Integer.parseInt(partes[0]),
                        partes[1],
                        partes[2],
                        Integer.parseInt(partes[3]),
                        Integer.parseInt(partes[4])
                ));
            }
        } catch (IOException e) {
            System.out.println("Error al leer productos: " + e.getMessage());
        }
        return productos;
    }

    private static List<Cliente> leerClientes() {
        List<Cliente> clientes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CLIENTES_FILE))) {
            String linea = br.readLine();
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                clientes.add(new Cliente(Integer.parseInt(partes[0]), partes[1], partes[2]));
            }
        } catch (IOException e) {
            System.out.println("Error al leer clientes: " + e.getMessage());
        }
        return clientes;
    }

    private static List<Pedido> leerPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PEDIDOS_FILE))) {
            String linea = br.readLine();
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                pedidos.add(new Pedido(
                        Integer.parseInt(partes[0]),
                        Integer.parseInt(partes[1]),
                        Integer.parseInt(partes[2]),
                        Integer.parseInt(partes[3]),
                        partes[4]
                ));
            }
        } catch (IOException e) {
            System.out.println("Error al leer pedidos: " + e.getMessage());
        }
        return pedidos;
    }

    private static void guardarClientes(List<Cliente> clientes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CLIENTES_FILE))) {
            bw.write("id,nombre,email\n");
            for (Cliente c : clientes) {
                bw.write(c.id + "," + c.nombre + "," + c.email + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar clientes: " + e.getMessage());
        }
    }

    private static void guardarTotalVentas(List<VentaTotal> ventas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TOTAL_VENTAS_FILE))) {
            bw.write("producto_id,nombre_producto,total\n");
            for (VentaTotal v : ventas) {
                bw.write(v.producto_id + "," + v.nombre_producto + "," + (int) v.total + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar ventas: " + e.getMessage());
        }
    }

    private static void ordenarQuickSort(List<Producto> lista, int inicio, int fin) {
        if (inicio < fin) {
            int pivote = particionarProductos(lista, inicio, fin);
            ordenarQuickSort(lista, inicio, pivote - 1);
            ordenarQuickSort(lista, pivote + 1, fin);
        }
    }

    private static int particionarProductos(List<Producto> lista, int inicio, int fin) {
        Producto pivote = lista.get(fin);
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            if (lista.get(j).precio < pivote.precio) {
                i++;
                intercambiarProductos(lista, i, j);
            }
        }
        intercambiarProductos(lista, i + 1, fin);
        return i + 1;
    }

    private static void intercambiarProductos(List<Producto> lista, int i, int j) {
        Producto temp = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, temp);
    }

    private static void ordenarVentasQuickSort(List<VentaTotal> lista, int inicio, int fin) {
        if (inicio < fin) {
            int pivote = particionarVentas(lista, inicio, fin);
            ordenarVentasQuickSort(lista, inicio, pivote - 1);
            ordenarVentasQuickSort(lista, pivote + 1, fin);
        }
    }

    private static int particionarVentas(List<VentaTotal> lista, int inicio, int fin) {
        VentaTotal pivote = lista.get(fin);
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            if (lista.get(j).total > pivote.total) {
                i++;
                intercambiarVentas(lista, i, j);
            }
        }
        intercambiarVentas(lista, i + 1, fin);
        return i + 1;
    }

    private static void intercambiarVentas(List<VentaTotal> lista, int i, int j) {
        VentaTotal temp = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, temp);
    }

    private static void ordenarStringQuickSort(List<String> lista, int inicio, int fin) {
        if (inicio < fin) {
            int pivote = particionarString(lista, inicio, fin);
            ordenarStringQuickSort(lista, inicio, pivote - 1);
            ordenarStringQuickSort(lista, pivote + 1, fin);
        }
    }

    private static int particionarString(List<String> lista, int inicio, int fin) {
        String pivote = lista.get(fin);
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            if (lista.get(j).compareTo(pivote) < 0) {
                i++;
                intercambiarString(lista, i, j);
            }
        }
        intercambiarString(lista, i + 1, fin);
        return i + 1;
    }

    private static void intercambiarString(List<String> lista, int i, int j) {
        String temp = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, temp);
    }

    static class Producto {
        int id, precio, stock;
        String nombre, categoria;

        Producto(int id, String nombre, String categoria, int precio, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precio = precio;
            this.stock = stock;
        }
    }

    static class Cliente {
        int id;
        String nombre, email;

        Cliente(int id, String nombre, String email) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
        }
    }

    static class Pedido {
        int id, cliente_id, producto_id, cantidad;
        String fecha;

        Pedido(int id, int cliente_id, int producto_id, int cantidad, String fecha) {
            this.id = id;
            this.cliente_id = cliente_id;
            this.producto_id = producto_id;
            this.cantidad = cantidad;
            this.fecha = fecha;
        }
    }

    static class VentaTotal {
        int producto_id;
        String nombre_producto;
        double total;

        VentaTotal(int producto_id, String nombre_producto, double total) {
            this.producto_id = producto_id;
            this.nombre_producto = nombre_producto;
            this.total = total;
        }
    }
}