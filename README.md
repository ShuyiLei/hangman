This project is hosted by a embedded Tomcat 8.5 Server.

JDK7.0+ is required for this project, please install JDK on your computer.

Maven is required for this project. If you don't have Maven installed in your
computer, please install one first.
For example, on Ubuntu, run:
```
sudo apt update
sudo apt install Maven
```

1. Exact the .tar.gz file to the place you want to run the project
`tar xzvf hangman.tar.gz <destination>`

2. Change current directory to the destination
`cd <destination>/hangman`

3. Use Maven to compile the project
`mvn package`

4. Run the script
`sh target/bin/webapp`
