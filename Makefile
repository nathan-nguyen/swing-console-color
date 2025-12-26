run-client:
	mvn clean compile && mvn exec:java -Dexec.mainClass="com.noiprocs.SwingApp" -Dexec.args="pc noiprocs client localhost 8080"
