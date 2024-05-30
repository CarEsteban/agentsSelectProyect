package com.pruebas.pruebaProyectoDatos;

import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        try (EmbeddedNeo4j neo4j = new EmbeddedNeo4j("bolt://localhost:7687", "neo4j", "123456789")) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Menú Principal");
                System.out.println("1. Crear usuario");
                System.out.println("2. Iniciar sesión");
                System.out.println("3. Salir");
                System.out.print("Elige una opción: ");
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea

                switch (opcion) {
                    case 1:
                        crearUsuario(neo4j, scanner);
                        break;
                    case 2:
                        iniciarSesion(neo4j, scanner);
                        break;
                    case 3:
                        System.out.println("Saliendo...");
                        return;
                    default:
                        System.out.println("Opción no válida. Inténtalo de nuevo.");
                }
            }
        }
    }

    private static void crearUsuario(EmbeddedNeo4j neo4j, Scanner scanner) {
        System.out.print("Introduce el nombre del usuario: ");
        String nombre = scanner.nextLine();
        System.out.print("Introduce la contraseña: ");
        String contrasena = scanner.nextLine();

        neo4j.crearUsuario(nombre, contrasena);

        System.out.println("Introduce los personajes que suele usar (separados por comas): ");
        String personajesInput = scanner.nextLine();
        String[] personajes = personajesInput.split(",");
        for (String personaje : personajes) {
            neo4j.agregarRelacionUsuarioPersonaje(nombre, personaje.trim());
        }

        System.out.println("Usuario creado exitosamente.");
    }

    private static void iniciarSesion(EmbeddedNeo4j neo4j, Scanner scanner) {
        System.out.print("Introduce el nombre del usuario: ");
        String nombre = scanner.nextLine();
        System.out.print("Introduce la contraseña: ");
        String contrasena = scanner.nextLine();

        boolean loginExitoso = neo4j.iniciarSesion(nombre, contrasena);
        if (loginExitoso) {
            System.out.println("Inicio de sesión exitoso.");
            mostrarPersonajesUsados(neo4j, nombre);
            menuRecomendaciones(neo4j, scanner, nombre);
        } else {
            System.out.println("Nombre de usuario o contraseña incorrectos.");
        }
    }

    private static void mostrarPersonajesUsados(EmbeddedNeo4j neo4j, String usuario) {
        List<String> personajes = neo4j.obtenerPersonajesUsadosPorUsuario(usuario);
        System.out.println("Personajes que " + usuario + " suele usar: " + personajes);
    }

    private static void menuRecomendaciones(EmbeddedNeo4j neo4j, Scanner scanner, String usuario) {
        while (true) {
            System.out.println("Menú de Recomendaciones");
            System.out.println("1. Recomendar agentes por rol");
            System.out.println("2. Recomendar agentes por habilidad");
            System.out.println("3. Recomendar usuarios por personajes en común");
            System.out.println("4. Salir");
            System.out.print("Elige una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea

            switch (opcion) {
                case 1:
                    recomendarPorRol(neo4j, scanner, usuario);
                    break;
                case 2:
                    recomendarPorHabilidad(neo4j, scanner, usuario);
                    break;
                case 3:
                    recomendarUsuarios(neo4j, scanner, usuario);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Opción no válida. Inténtalo de nuevo.");
            }
        }
    }

    private static void recomendarPorRol(EmbeddedNeo4j neo4j, Scanner scanner, String usuario) {
        Map<String, Long> rolesCount = neo4j.contarRolesPorUsuario(usuario);
        if (rolesCount.isEmpty()) {
            System.out.println("No se encontraron roles para el usuario.");
            return;
        }

        long maxFrecuencia = rolesCount.values().stream().max(Long::compare).orElse(0L);
        List<String> rolesMasFrecuentes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : rolesCount.entrySet()) {
            if (entry.getValue() == maxFrecuencia) {
                rolesMasFrecuentes.add(entry.getKey());
            }
        }

        List<String> agentes = neo4j.recomendarAgentesPorRoles(usuario, rolesMasFrecuentes);
        System.out.println("Recomendaciones de agentes por roles " + rolesMasFrecuentes + ": " + agentes);
    }

    private static void recomendarPorHabilidad(EmbeddedNeo4j neo4j, Scanner scanner, String usuario) {
        Map<String, Long> rolesCount = neo4j.contarRolesPorUsuario(usuario);
        if (rolesCount.isEmpty()) {
            System.out.println("No se encontraron roles para el usuario.");
            return;
        }

        long maxFrecuencia = rolesCount.values().stream().max(Long::compare).orElse(0L);
        List<String> rolesMasFrecuentes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : rolesCount.entrySet()) {
            if (entry.getValue() == maxFrecuencia) {
                rolesMasFrecuentes.add(entry.getKey());
            }
        }

        Map<String, Long> habilidadesCount = neo4j.contarHabilidadesPorUsuario(usuario, rolesMasFrecuentes);
        if (habilidadesCount.isEmpty()) {
            System.out.println("No se encontraron habilidades para los roles más frecuentes.");
            return;
        }

        long maxHabilidadFrecuencia = habilidadesCount.values().stream().max(Long::compare).orElse(0L);
        List<String> habilidadesMasFrecuentes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : habilidadesCount.entrySet()) {
            if (entry.getValue() == maxHabilidadFrecuencia) {
                habilidadesMasFrecuentes.add(entry.getKey());
            }
        }

        for (String habilidad : habilidadesMasFrecuentes) {
            List<String> agentes = neo4j.recomendarAgentesPorHabilidades(usuario, habilidad);
            System.out.println("Recomendaciones de agentes con habilidad " + habilidad + ": " + agentes);
        }
    }

    private static void recomendarUsuarios(EmbeddedNeo4j neo4j, Scanner scanner, String usuario) {
        List<String> usuarios = neo4j.obtenerUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("No se encontraron otros usuarios.");
            return;
        }

        System.out.println("Usuarios existentes:");
        for (String u : usuarios) {
            if (!u.equals(usuario)) {
                System.out.println(u);
            }
        }

        System.out.print("Introduce el nombre del otro usuario: ");
        String otroUsuario = scanner.nextLine();

        List<String> personajesEnComun = neo4j.recomendarUsuariosPorPersonajesEnComun(usuario, otroUsuario);
        System.out.println("Personajes en común con " + otroUsuario + ": " + personajesEnComun);
    }
}
