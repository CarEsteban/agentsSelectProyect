import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class EmbeddedNeo4j implements AutoCloseable {
    private final Driver driver;

    public EmbeddedNeo4j(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void crearUsuario(String nombre, String contrasena) {
        try (Session session = driver.session()) {
            String query = "CREATE (u:Usuario {nombre: $nombre, contrasena: $contrasena})";
            session.run(query, org.neo4j.driver.Values.parameters("nombre", nombre, "contrasena", contrasena));
        }
    }

    public void agregarRelacionUsuarioPersonaje(String usuario, String personaje) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:Usuario {nombre: $usuario}), (p:Personaje {nombre: $personaje}) " +
                           "CREATE (u)-[:USA]->(p)";
            session.run(query, org.neo4j.driver.Values.parameters("usuario", usuario, "personaje", personaje));
        }
    }

    public boolean iniciarSesion(String nombre, String contrasena) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:Usuario {nombre: $nombre, contrasena: $contrasena}) RETURN u";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("nombre", nombre, "contrasena", contrasena));
            return result.hasNext();
        }
    }

    public Map<String, Long> contarRolesPorUsuario(String usuario) {
        Map<String, Long> rolesCount = new HashMap<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u:Usuario {nombre: $usuario})-[:USA]->(p:Personaje)-[:ES_UN]->(r:Rol) " +
                           "RETURN r.nombre AS rol, count(*) AS frecuencia";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("usuario", usuario));
            while (result.hasNext()) {
                Record record = result.next();
                rolesCount.put(record.get("rol").asString(), record.get("frecuencia").asLong());
            }
        }
        return rolesCount;
    }

    public List<String> recomendarAgentesPorRol(String usuario, String rol) {
        List<String> agentes = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u:Usuario {nombre: $usuario})-[:USA]->(p:Personaje) " +
                           "WITH collect(p.nombre) AS usados " +
                           "MATCH (p:Personaje)-[:ES_UN]->(:Rol {nombre: $rol}) " +
                           "WHERE NOT p.nombre IN usados " +
                           "RETURN p.nombre AS nombre";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("usuario", usuario, "rol", rol));
            while (result.hasNext()) {
                Record record = result.next();
                agentes.add(record.get("nombre").asString());
            }
        }
        return agentes;
    }

    public List<String> recomendarAgentesPorHabilidad(String usuario, String habilidad) {
        List<String> agentes = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u:Usuario {nombre: $usuario})-[:USA]->(p:Personaje)-[:TIENE_HABILIDAD]->(h:Habilidad {nombre: $habilidad}) " +
                           "RETURN p.nombre AS nombre";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("usuario", usuario, "habilidad", habilidad));
            while (result.hasNext()) {
                Record record = result.next();
                agentes.add(record.get("nombre").asString());
            }
        }
        return agentes;
    }

    public List<String> recomendarUsuariosPorPersonajesEnComun(String usuario1, String usuario2) {
        List<String> personajesEnComun = new ArrayList<>();
        try (Session session = driver.session()) {
            String query = "MATCH (u1:Usuario {nombre: $usuario1})-[:USA]->(p:Personaje)<-[:USA]-(u2:Usuario {nombre: $usuario2}) " +
                           "RETURN DISTINCT p.nombre AS nombre";
            Result result = session.run(query, org.neo4j.driver.Values.parameters("usuario1", usuario1, "usuario2", usuario2));
            while (result.hasNext()) {
                Record record = result.next();
                personajesEnComun.add(record.get("nombre").asString());
            }
        }
        return personajesEnComun;
    }

    public Driver getDriver() {
        return driver;
    }
}