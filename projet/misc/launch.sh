#Permet d'accéder plus facilement à nos classes
export CLASSPATH=.:../lib/jade/lib/jade.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes

#Compile et exécute l'agent Preneur
javac -d ../classes ../preneur/Preneur.java
java jade.Boot -agents "A1:preneur.Preneur(Pierre)"