default:
	javac -cp ./lib -d ./out src/com/company/Main.java

run:
	java -cp ./out com/company/Main
