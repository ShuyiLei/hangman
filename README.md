This project is hosted by a embedded Tomcat 8.5 Server. You can download the tarball file at https://github.com/ShuyiLei/hangman/releases

JDK7.0+ is required for this project, please install JDK on your computer.

Maven is required for this project. If you don't have Maven installed in your
computer, please install one first.
For example, on Ubuntu, run:
```
sudo apt update
sudo apt install maven
```

## Run the project

1. Exact the .tar.gz file to the place you want to run the project
```
tar xzvf hangman-<version number>.tar.gz -C <destination>
```

2. Change current directory to the destination
```
cd <destination>/hangman-<version number>
```

3. Use Maven to compile the project
```
mvn package
```

4. Run the script
```
sh target/bin/webapp
```

## Test the project

1. Follow the step 1 to 3 of the **Run the project** part.

2. Run the script
```
sh target/bin/tests
```
