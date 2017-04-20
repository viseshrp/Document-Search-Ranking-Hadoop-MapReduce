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
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/*Created by Viseshprasad Rajendraprasad
vrajend1@uncc.edu
*/

public class Rank extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(Rank.class);

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Rank(), args);
		System.exit(res);
	}

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), " rank ");
		job.setJarByClass(this.getClass());

		FileInputFormat.addInputPaths(job, args[0]);
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setMapOutputKeyClass(DoubleWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		job.setSortComparatorClass(DescendingComparator.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class Map extends Mapper<LongWritable, Text, DoubleWritable, Text> {

		public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
			String[] temp = lineText.toString().split("\\s+"); // stores the
																// filename and
																// tfidf

			// use tfidf as key and filename as value to help sort
			context.write(new DoubleWritable(Double.valueOf(temp[1])), new Text(temp[0]));

		}
	}

	public static class Reduce extends Reducer<DoubleWritable, Text, Text, DoubleWritable> {
		@Override
		public void reduce(DoubleWritable tfidf, Iterable<Text> fileNames, Context context)
				throws IOException, InterruptedException {

			// loop through the list of files for each value
			// in case multiple files have same value.
			for (Text file : fileNames) {
				context.write(file, tfidf);
			}
		}
	}

	// custom comparator for sorting tfidf values in descending order
	public static class DescendingComparator extends WritableComparator {

		public DescendingComparator() {
			super();
			// TODO Auto-generated constructor stub
		}

		@Override
		public int compare(byte[] arg0, int arg1, int arg2, byte[] arg3, int arg4, int arg5) {
			// TODO Auto-generated method stub

			double tfidf1 = WritableComparator.readDouble(arg0, arg1);
			double tfidf2 = WritableComparator.readDouble(arg3, arg4);
			if (tfidf1 > tfidf2) {
				return -1;
			} else if (tfidf1 < tfidf2) {
				return 1;
			}
			return 0;
		}
	}
}
