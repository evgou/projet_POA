# export CLASSPATH=.:../lib/jade/lib/jade2.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH
javac -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar" \
      -d ../classes ../misc/FishMarketPerformatif.java \
                    ../vendeur/Vendeur.java \
                    ../vendeur/VendeurGUI.java  \
                    ../marche/Marche.java  \
                    ../marche/MarcheGUI.java
java -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar:../classes" jade.Boot -gui -agents "Pierre:vendeur.Vendeur(Rocher);Paul:marche.Marche(Paulo)"