# the Java compiler
JC = javac

# the Java runtime
JVM = java

# the compiler flags (not applicable for Java, but you can add other options if needed)
JFLAGS =

# the source files
SRC = $(wildcard *.java)

# the class files
CLS = $(SRC:.java=.class)

# the main class (change "Main" to the name of your main Java class)
MAIN_CLASS = Main

# the default target
all: $(CLS)

# the class files depend on the source files
$(CLS): $(SRC)
	$(JC) $(JFLAGS) $(SRC)

# the clean target
clean:
	rm -f $(CLS)

# the run target
run: $(CLS)
	$(JVM) $(MAIN_CLASS)

# the zip target
zip:
	zip -r project.zip $(SRC) Makefile

# the help target
help:
	@echo "all:     builds the project"
	@echo "clean:   removes the class files"
	@echo "run:     runs the Java program"
	@echo "zip:     creates a zip file for the project"
	@echo "help:    shows this help"

# the phony targets
.PHONY: all clean run zip help
