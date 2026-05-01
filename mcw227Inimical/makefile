APP_NAME = DatabaseCLI.jar
SRC_FOLDER = src
SOURCES = $(wildcard $(SRC_FOLDER)/*.java)
CLASSES = $(patsubst $(SRC_FOLDER)/%.java, %.class, $(SOURCES))

run_jar: $(APP_NAME)
	java -jar DatabaseCLI.jar

run: $(APP_NAME)
	java -cp "ojdbc11.jar:." DatabaseCLI

$(APP_NAME): $(SOURCES)
	javac -cp "ojdbc11.jar:." -d . $(SOURCES)
	jar cfmv $(APP_NAME) Manifest.MF $(CLASSES)

clean:
	rm -f *.class
	
true_clean:
	rm -f *.class $(APP_NAME)
