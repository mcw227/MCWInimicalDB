APP_NAME = DatabaseCLI.jar
CLASSES = DatabaseCLI.class Customer.class Card.class

%.class: %.java
	javac $< 

run: $(CLASSES)
	java -cp "ojdbc11.jar:." DatabaseCLI

run_jar: $(APP_NAME)
	java -jar DatabaseCLI.jar

$(APP_NAME): $(CLASSES)
	jar cfmv $(APP_NAME) Manifest.MF $(CLASSES)

clean:
	rm -f *.class DatabaseCLI.jar
