package org.myorg;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
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

public class Search extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(Search.class);

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Search(), args);
		System.exit(res);
	}

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), " search ");
		job.setJarByClass(this.getClass());

		Configuration configuration = job.getConfiguration();
		configuration.set("query", args[2]);

		FileInputFormat.addInputPaths(job, args[0]);
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, DoubleWritable> {

		public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
			String[] lineInputSplit = lineText.toString().split("\\s+"); // splits
																			// and
																			// stores
																			// the
																			// word
																			// +
																			// filename
																			// and
																			// tfidf
			String[] temp = lineInputSplit[0].split("#####"); // splits and
																// stores the
																// word and
																// filename
			String currentWord = temp[0]; // word

			String query = context.getConfiguration().get("query");

			String[] queryArray = query.split("\\s+");

			for (String queryWord : queryArray) {
				if (queryWord.isEmpty()) {
					continue;
				}

				if (queryWord.equals(currentWord))
					context.write(new Text(temp[1]), new DoubleWritable(Double.valueOf(lineInputSplit[1])));
			}
		}
	}

	public static class Reduce extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
		@Override
		public void reduce(Text fileName, Iterable<DoubleWritable> counts, Context context)
				throws IOException, InterruptedException {
			double sum = 0;
			for (DoubleWritable count : counts) {
				sum += count.get();
			}
			context.write(fileName, new DoubleWritable(sum));
		}
	}
}
