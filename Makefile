default:
	javac -cp ./lib:./out -d ./out src/com/company/util/*.java
	javac -cp ./lib:./out -d ./out src/com/company/option/*.java
	javac -cp ./lib:./out -d ./out src/com/company/*.java

run:
	java -cp ./out com/company/Main

clean:
	rm -rf out/*
