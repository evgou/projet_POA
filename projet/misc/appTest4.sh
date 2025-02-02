# export CLASSPATH=.:../lib/jade/lib/jade2.jar:../lib/jade/lib/commons-codec/commons-codec-1.3.jar:../classes:$CLASSPATH
javac -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar" \
      -d ../classes ../misc/*.java \
                    ../vendeur/*.java \
                    ../marche/*.java
java -Djava.util.logging.config.file="logging.properties" -classpath "../lib/jade.jar:../lib/commons-codec/commons-codec-1.3.jar:../classes" jade.Boot -agents "Vincent:vendeur.Vendeur(Vincent);Victor:vendeur.Vendeur(Victor);market:marche.Marche;Pierre:preneur.Preneur(Pierre);Paul:preneur.Preneur(Paul)"