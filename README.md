# swing-console-color

## Setup environment
- Require `mvn install` in `network-client-server` and `console-color`.

## Run
```sh
# Start client with Swing UI
mvn compile exec:java -Dexec.mainClass="com.noiprocs.SwingApp" -Dexec.args="pc noiprocs client localhost 8080"

# Start client with console
mvn compile exec:java -Dexec.mainClass="com.noiprocs.App" -Dexec.args="pc noiprocs client localhost 8080"
```
