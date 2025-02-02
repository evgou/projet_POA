#Permet d'accéder plus facilement à nos classes
export CLASSPATH=.:../lib/jade/lib/jade.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH

# Compile et exécute l'agent PreneurAgent
javac -d ../classes ../preneur/* ../vendeur/* ../marche/* #../misc/FishMarketPerformatif.java
java jade.Boot -gui -agents "market:marche.Marche();Pierre:preneur.Preneur(Rocher);Paul:preneur.Preneur(Paulo);Vincent:vendeur.Vendeur(Vincent);"

# Compile et exécute l'agent Marché + Vendeur
#javac -d ../classes ../vendeur/* ../marche/*
#java jade.Boot -gui -agents "Manu:marche.MarcheAgent(Manu);Victor:vendeur.VendeurAgent(Victor);Vincent:vendeur.VendeurAgent(Vincent)"