all: DatabaseCLI.class Customer.class

%.class: %.java
	javac $< 

run: all
	java -cp "ojdbc11.jar:." DatabaseCLI

run_jar: jar
	java -jar DatabaseCLI.jar

jar: all
	jar cfmv DatabaseCLI.jar Manifest.MF DatabaseCLI.class Customer.class

clean:
	rm -f *.class DatabaseCLI.jar
