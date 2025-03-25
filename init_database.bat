@echo off
echo Initialisation de la base de données Missié Moustass...

rem Supprimer la base de données existante si elle existe
if exist users.db del users.db

rem Compiler la classe d'initialisation
javac -cp "lib/*" src/InitDatabase.java -d bin

rem Exécuter la classe d'initialisation
java -cp "lib/*;bin" InitDatabase