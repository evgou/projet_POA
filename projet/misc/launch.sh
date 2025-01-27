export CLASSPATH=.:../lib/jade/lib/jade.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH
javac -d ../classes ../vendeur/Vendeur.java ../marche/Marche.java
#java jade.Boot -gui -agents "Pierre:vendeur.Vendeur(Rocher);Paul:marche.Marche(Paulo)"