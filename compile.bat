@echo off
echo Compilation du projet Missie Moustass...

rem Créer le répertoire bin s'il n'existe pas
if not exist bin mkdir bin

rem Compiler les classes d'authentification (en excluant les tests)
echo Compilation des classes d'authentification...
javac -Xlint:none -cp "lib/*;bin" -d bin src/Auth/AES.java src/Auth/Admin.java src/Auth/AudioRecorder.java src/Auth/Connexion.java src/Auth/Inscription.java src/Auth/Reset.java src/Auth/Reset2.java src/Auth/SHA.java

rem Compiler les classes audio
echo Compilation des classes audio...
javac -Xlint:none -cp "lib/*;bin" -d bin src/com/barbichetz/audio/*.java

rem Compiler la classe principale
echo Compilation de la classe principale...
javac -Xlint:none -cp "lib/*;bin;src" -d bin src/main/AudioRecorder.java

echo Compilation terminée. 