CLASSPATH=./lib/postgresql-9.4.1208.jre6.jar:./out

default:
	javac -cp $(CLASSPATH) -d ./out src/com/company/util/*.java
	javac -cp $(CLASSPATH) -d ./out src/com/company/option/*.java
	javac -cp $(CLASSPATH) -d ./out src/com/company/*.java

run:
	java -cp $(CLASSPATH) com/company/Main

clean:
	rm -rf out/*
