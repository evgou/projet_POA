# export CLASSPATH=.:../lib/jade/lib/jade2.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH
javac -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar" \
      -d ../classes ../misc/FishMarketPerformatif.java \
                    ../vendeur/Vendeur.java \
                    ../vendeur/VendeurGUI.java  \
                    ../marche/Marche.java  \
                    ../marche/MarcheGUI.java
java -Djava.util.logging.config.file="logging.properties" -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar:../classes" jade.Boot -agents "Vincent:vendeur.Vendeur(Vince);market:marche.Marche"