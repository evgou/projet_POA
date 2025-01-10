#Permet d'accéder plus facilement à nos classes
export CLASSPATH=.:../lib/jade/lib/jade.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes

#Compile et exécute l'agent Preneur
#javac -d ../classes ../preneur/PreneurAgent.java
#java jade.Boot -agents "A1:preneur.Preneur(Pierre)"

#Compile et exécute l'agent Market
#javac -d ../classes ../marche/Marche.java
#java jade.Boot -gui -agents "A1:marche.Marche(Pierre)"

# Compile et exécute l'agent PreneurAgent
javac -d ../classes ../preneur/PreneurAgent.java ../preneur/PreneurAgentGUI.java
java jade.Boot -gui -agents "A1:preneur.PreneurAgent(Rocher)"