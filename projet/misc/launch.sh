#Permet d'accéder plus facilement à nos classes
export CLASSPATH=.:../lib/jade/lib/jade.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH

#Compile et exécute l'agent Preneur
#javac -d ../classes ../preneur/PreneurAgent.java
#java jade.Boot -agents "A1:preneur.Preneur(Pierre)"

#Compile et exécute l'agent Market
#javac -d ../classes ../marche/Marche.java
#java jade.Boot -gui -agents "A1:marche.Marche(Pierre)"

javac -d ../classes ./Prix.java

# Compile et exécute l'agent PreneurAgent
javac -d ../classes ../preneur/PreneurAgent.java ../preneur/PreneurAgentGUI.java ../vendeur/* ../marche/* #../misc/FishMarketPerformatif.java
java jade.Boot -gui -agents "market:marche.MarcheAgent();Pierre:preneur.PreneurAgent(Rocher);Paul:preneur.PreneurAgent(Paulo);Vincent:vendeur.VendeurAgent(Vincent);"

# Compile et exécute l'agent Marché + Vendeur
#javac -d ../classes ../vendeur/* ../marche/*
#java jade.Boot -gui -agents "Manu:marche.MarcheAgent(Manu);Victor:vendeur.VendeurAgent(Victor);Vincent:vendeur.VendeurAgent(Vincent)"