git pull
javac -cp ../client-combined-3.141.59.jar scraper.java
java -cp .:../client-combined-3.141.59.jar:../libs/byte-buddy-1.8.15.jar:../libs/commons-exec-1.3.jar:../libs/guava-25.0-jre.jar:../libs/okhttp-3.11.0.jar:../libs/okio-1.14.0.jar scraper $1 $2 $3 $4
git add -A
git commit -m "updates"
git push