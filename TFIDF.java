package org.myorg;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.myorg.TermFrequency;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/*Created by Viseshprasad Rajendraprasad
vrajend1@uncc.edu
*/

public class TFIDF extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(TFIDF.class);

	public static void main(String[] args) throws Exception {
		// Chain the term frequency and the TFIDF jobs using Toolrunner
		int res_termfrequency = ToolRunner.run(new TermFrequency(), args);
		int res_tfidf = ToolRunner.run(new TFIDF(), args);
		System.exit(res_tfidf);
	}

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), " tfidf ");

		Configuration configuration = job.getConfiguration(); // create a
																// configuration
																// reference

		// get file count in the input folder
		FileSystem fs = FileSystem.get(configuration);
		Path path = new Path(args[0]);
		ContentSummary cs = fs.getContentSummary(path);
		long fileCount = cs.getFileCount();

		configuration.setInt("totalDocuments", (int) fileCount); // use
																	// configuration
																	// object to
																	// pass file
																	// count

		job.setJarByClass(this.getClass());
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		// Explicitly set key and value types of map and reduce output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {

			Text currentWord = new Text();
			String[] lineInputSplit = lineText.toString().split("#####"); // split
																			// line
																			// input
																			// to
																			// get
																			// the
																			// word
			currentWord = new Text(lineInputSplit[0]);

			// reformat and combine the filename and term frequency of word in
			// that file
			String value = lineInputSplit[1].replaceAll("\\s+", "=");

			context.write(currentWord, new Text(value));
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, DoubleWritable> {
		@Override
		public void reduce(Text word, Iterable<Text> postingsList, Context context)
				throws IOException, InterruptedException {

			// hashmap to store filename/key and term frequency/value
			// Hashmap has been used because it was difficult get the output
			// by looping through the Iterable twice
			HashMap<String, String> hashMap = new HashMap<>();

			int documentFrequency = 0; // number of documents that contain the
										// word

			// Loop through postings list to accumulate and find the document
			// frequency
			for (Text count : postingsList) {
				String[] temp = count.toString().split("=");
				hashMap.put(temp[0], temp[1]);
				documentFrequency++;
			}

			// Loop through hashmap to get filename and calculate corresponding
			// TFIDF from TF.
			for (String key : hashMap.keySet()) {
				// get termfrequency with filename from map
				double termFrequency = Double.parseDouble(hashMap.get(key));

				// calculate IDF as per formula
				double IDF = Math.log10(1.0 + (Double.valueOf(context.getConfiguration().get("totalDocuments"))
						/ Double.valueOf(documentFrequency)));

				double tfidf = termFrequency * IDF; // calculate TFIDF

				// reducer output key to include word and filename as required
				String reducerOutputKey = word.toString() + "#####" + key;

				// write to output
				context.write(new Text(reducerOutputKey), new DoubleWritable(tfidf));
			}
		}
	}
}
