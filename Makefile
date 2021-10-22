start:
	mvn compile exec:java
build:
	mvn compile assembly:single
clean:
	mvn clean
	rm -rf bin
test:
	mvn test
install:
	chmod +x target/worldsorganizer-*-jar-with-dependencies.jar
	mkdir -p bin
	cp target/worldsorganizer-*-jar-with-dependencies.jar bin
deploy:
	make clean build test install
