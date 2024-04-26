package demo;

import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.QueryConfig;
import org.neo4j.driver.RoutingControl;

public class App {

    private static final String URI = "<neo4j+s://42ccd2dc.databases.neo4j.io>";
    private static final String USER = "<neo4j>";
    private static final String PASSWORD = "<Lc47HWTZO6HLPKWAftFPhNetzhYBfH_AbjtNv7gY-ew>";

    public static void main(String... args) {

        try (var driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD))) {

            List<Map> people = List.of(
                Map.of("name", "Alice", "age", 42, "friends", List.of("Bob", "Peter", "Anna")),
                Map.of("name", "Bob", "age", 19),
                Map.of("name", "Peter", "age", 50),
                Map.of("name", "Anna", "age", 30)
            );

            try {

                //Create some nodes
                people.forEach(person -> {
                    var result = driver.executableQuery("CREATE (p:Person {name: $person.name, age: $person.age})")
                        .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                        .withParameters(Map.of("person", person))
                        .execute();
                });

                // Create some relationships
                people.forEach(person -> {
                    if(person.containsKey("friends")) {
                        var result = driver.executableQuery("""
                            MATCH (p:Person {name: $person.name})
                            UNWIND $person.friends AS friend_name
                            MATCH (friend:Person {name: friend_name})
                            CREATE (p)-[:KNOWS]->(friend)
                             """)
                            .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                            .withParameters(Map.of("person", person))
                            .execute();
                    }
                });

                // Retrieve Alice's friends who are under 40
                var result = driver.executableQuery("""
                    MATCH (p:Person {name: $name})-[:KNOWS]-(friend:Person)
                    WHERE friend.age < $age
                    RETURN friend
                     """)
                    .withConfig(QueryConfig.builder()
                        .withDatabase("neo4j")
                        .withRouting(RoutingControl.READ)
                        .build())
                    .withParameters(Map.of("name", "Alice", "age", 40))
                    .execute();

                // Loop through results and do something with them
                result.records().forEach(r -> {
                    System.out.println(r);  // doesn't unwrap properties, Record<{friend: node<4>}>, nor .fields() does
                    // python shows <Record friend=<Node element_id='4:5bad7cf2-d17e-4d07-9266-b88c8720ebd2:4' labels=frozenset({'Person'}) properties={'name': 'Bob', 'age': 19}>>
                });

                // Summary information
                System.out.printf("The query %s returned %d records in %d ms.%n",
                    result.summary().query(), result.records().size(),
                    result.summary().resultAvailableAfter(TimeUnit.MILLISECONDS));

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}