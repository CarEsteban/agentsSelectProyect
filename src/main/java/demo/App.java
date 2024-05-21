
/**
 * 
 */
package demo;

import java.util.LinkedList;
import java.util.Scanner;


/**
 * @author MAAG
 *
 */
public class App {

    private static final String URI = "<neo4j+s://42ccd2dc.databases.neo4j.io>";
    private static final String USER = "<neo4j>";
    private static final String PASSWORD = "<Lc47HWTZO6HLPKWAftFPhNetzhYBfH_AbjtNv7gY-ew>";

    public static void main(String... args) {

        try (var driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD))) {

            

            try {
                //CREACIÓN DE NODOS
                people.forEach(person -> {
                    var result = driver.executableQuery("CREATE (p:Person {name: $Jett, role: Duelista})")
                    var result = driver.executableQuery("CREATE (p:Person {name: $Breach, role: Iniciador})")
                    var result = driver.executableQuery("CREATE (p:Person {name: $Viper, role: Controlador})")
                    var result = driver.executableQuery("CREATE (p:Person {name: $Clove, role: Controlador})")
                        .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                        .withParameters(Map.of("person", person))
                        .execute();
                });

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}