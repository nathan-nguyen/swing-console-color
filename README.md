# swing-console-color

## Setup environment
- Require `mvn install` in `network-client-server` and `console-color`.

## Build
```sh
mvn compile
```

## Run
```sh
# Start client with Swing UI
mvn exec:java -Dexec.mainClass="com.noiprocs.SwingApp" -Dexec.args="pc noiprocs client localhost 8080"

# Start client with console
mvn exec:java -Dexec.mainClass="com.noiprocs.App" -Dexec.args="pc noiprocs client localhost 8080"

# Client yaiba
mvn compile exec:java -Dexec.mainClass="com.noiprocs.App" -Dexec.args="pc yaiba client localhost 8080"
```
