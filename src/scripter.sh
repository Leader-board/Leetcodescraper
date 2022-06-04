# make sure that things are as expected
echo "Weekly start: $1";
echo "Weekly end: $2";
echo "Biweekly start: $3";
echo "Biweekly end: $4";
echo "Number of threads: $5";
git pull
javac -cp ../client-combined-3.141.59.jar scraper.java
java -cp .:../client-combined-3.141.59.jar:../libs/byte-buddy-1.8.15.jar:../libs/commons-exec-1.3.jar:../libs/guava-25.0-jre.jar:../libs/okhttp-3.11.0.jar:../libs/okio-1.14.0.jar scraper $1 $2 $3 $4 $5
javac combinedanalysis.java
java combinedanalysis > ../combinedresult.txt
git add -A
git commit -m "updates"
git push