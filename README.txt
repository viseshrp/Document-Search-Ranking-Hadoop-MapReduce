


----------------------------------------------------------------------------
----------------------------------------------------------------------------
Files Included With This Project:
----------------------------------------------------------------------------
----------------------------------------------------------------------------

DocWordCount.java
TermFrequency.java  
TFIDF.java
Search.java
Rank.java

DocWordCount.out
TermFrequency.out
TFIDF.out
query1.out
query2.out
query1-rank.out
query2-rank.out


----------------------------------------------------------------------------
----------------------------------------------------------------------------
Run instructions example:
----------------------------------------------------------------------------
----------------------------------------------------------------------------

----------------------------------------------------------------------------
Please make sure the directories that are written to at each step,i.e output
directories DO NOT exist. If they do, run:
hadoop fs -rm -r insert_dir_path_to_be_deleted
----------------------------------------------------------------------------


Assuming the following:

All java files are in the current directory and you have a "build" directory present inside the current directory.
input directory containing files is: /user/vpcl/wordcount/input
output directory for WordCount and TermFrequency is: /user/vpcl/wordcount/output. This is also the intermediate directory
for the TFIDF run and the directory from which the TFIDF job takes its input.
output directory for TFIDF is: /user/vpcl/wordcount/final
output directory for Search is: /user/vpcl/wordcount/searchout
input directory for Rank is: /user/vpcl/wordcount/rankinput - should contain one output of search job at a time
output directory for Rank is: /user/vpcl/wordcount/rankoutput

----------------------------------------------------------------------------
DocWordCount.java
----------------------------------------------------------------------------

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* DocWordCount.java -d build -Xlint
jar -cvf docwordcount.jar -C build/ .
hadoop jar docwordcount.jar org.myorg.DocWordCount /user/vpcl/wordcount/input /user/vpcl/wordcount/output

----------------------------------------------------------------------------
TermFrequency.java
----------------------------------------------------------------------------

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* TermFrequency.java -d build -Xlint
jar -cvf termfrequency.jar -C build/ .
hadoop jar termfrequency.jar org.myorg.TermFrequency /user/vpcl/wordcount/input /user/vpcl/wordcount/output

----------------------------------------------------------------------------
TFIDF.java (Also have to compile TermFrequency.java to help chaining)
----------------------------------------------------------------------------

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/*:. *.java -d build -Xlint
jar -cvf tfidf.jar -C build/ .
hadoop jar tfidf.jar org.myorg.TFIDF /user/vpcl/wordcount/input /user/vpcl/wordcount/output /user/vpcl/wordcount/final

----------------------------------------------------------------------------
Search.java
----------------------------------------------------------------------------

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* Search.java -d build -Xlint
jar -cvf search.jar -C build/ .
hadoop jar search.jar org.myorg.Search /user/vpcl/wordcount/final /user/vpcl/wordcount/searchout "insert_search_query_here"

----------------------------------------------------------------------------
Rank.java
----------------------------------------------------------------------------

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* Rank.java -d build -Xlint
jar -cvf rank.jar -C build/ .
hadoop jar rank.jar org.myorg.Rank /user/vpcl/wordcount/rankinput /user/vpcl/wordcount/rankoutput