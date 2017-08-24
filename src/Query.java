import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class Query {
	
	private final static int QUERY_NUM = 50;
	public static String[] output = new String[QUERY_NUM];
	public static QueryStructure[] queryResult = new QueryStructure[QUERY_NUM];
	
	public static void readQueryFile() {
		try {
			FileReader fr = new FileReader(Main.QUERY_FILE_PATH);
			BufferedReader br = new BufferedReader(fr);

			int i = 0;
			String temp;
			while ((temp = br.readLine()) != null) {
				if (temp.matches("<top>")) {
					// Get the query number
					temp = br.readLine();
					temp = temp.replaceAll("<num>\\sNumber:\\s", "");
					temp = temp.replaceAll("\\s</num>", "");
					output[i] = temp + " ";
					
					// Get the query content
					temp = br.readLine();
					temp = temp.replaceAll("<title>\\s", "");
					temp = temp.replaceAll("\\s+</title>", "");
					temp = temp.toLowerCase();
					temp = preprocessQuery(temp);
					output[i] += temp;
					
					i++;
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error occurs during processing query file:" + e.getMessage());
		}
	}
	
	// use the tf-idf weighting scheme
	public static void query() {
		HashMap<String, Integer> term_fs;
		int max_f;
		// For each query
		for (int i = 0; i < QUERY_NUM; i++) {
			queryResult[i] = new QueryStructure();
			
			// Hold the frequency of each term in that query
			term_fs = new HashMap<String, Integer>();
			max_f = 0;
			
			String[] t = output[i].split(" ");
			// Get query number
			queryResult[i].querID = t[0];
			// Get frequency of the terms in that query and maximum frequency
			for (int j = 1; j < t.length; j++) {
				if (!term_fs.containsKey(t[j])) {
					term_fs.put(t[j], 1);
					
					max_f = Indexing.findMax(1, max_f);
				} else {
					int f = term_fs.get(t[j]);
					f++;
					term_fs.replace(t[j], f);
					
					max_f = Indexing.findMax(f, max_f);
				}
			}
			// Get query and documents length
			double length = 0.0;
			for (String term : term_fs.keySet()) {
				if (Indexing.indexValue.containsKey(term)) {
					IndexStructure is = Indexing.indexValue.get(term);
					double tf = (double) term_fs.get(term) / max_f;
					double idf = Math.log(IndexStructure.getTotalDocsNum() / is.df) / Math.log(2);
					length += (tf * idf) * (tf * idf);
				}
			}
			length = Math.sqrt(length);
			Indexing.calculateDocumentsLength();
			// Calculate similarity
			queryResult[i].tweet_sims = new HashMap<String, Double>();
			for (String term : term_fs.keySet()) {
				if (Indexing.indexValue.containsKey(term)) {
					IndexStructure is = Indexing.indexValue.get(term);
					double tf_1 = (double) term_fs.get(term) / max_f;
					double idf = Math.log(IndexStructure.getTotalDocsNum() / is.df) / Math.log(2);
					for (String tweetID : is.tweetID_fs.keySet()) {
						double tf_2 = (double) is.tweetID_fs.get(tweetID) / Indexing.tweet_maxfs.get(tweetID);
						double sim;
						if (!queryResult[i].tweet_sims.containsKey(tweetID)) {
							sim = (tf_1 * idf) * (tf_2 * idf) / (length * Indexing.tweet_length.get(tweetID));
							queryResult[i].tweet_sims.put(tweetID, sim);
						} else {
							sim = queryResult[i].tweet_sims.get(tweetID);
							sim += (tf_1 * idf) * (tf_2 * idf) / (length * Indexing.tweet_length.get(tweetID));
							queryResult[i].tweet_sims.replace(tweetID, sim);
						}
					}
				}
			}
		}
		printQueryResult();
	}
	
	// use the modified tf-idf weighting scheme for query terms
	public static void modified_query() {
		HashMap<String, Integer> term_fs;
		int max_f;
		// For each query
		for (int i = 0; i < QUERY_NUM; i++) {
			queryResult[i] = new QueryStructure();
			
			// Hold the frequency of each term in that query
			term_fs = new HashMap<String, Integer>();
			max_f = 0;
			
			String[] t = output[i].split(" ");
			// Get query number
			queryResult[i].querID = t[0];
			// Get frequency of the terms in that query and maximum frequency
			for (int j = 1; j < t.length; j++) {
				if (!term_fs.containsKey(t[j])) {
					term_fs.put(t[j], 1);
					
					max_f = Indexing.findMax(1, max_f);
				} else {
					int f = term_fs.get(t[j]);
					f++;
					term_fs.replace(t[j], f);
					
					max_f = Indexing.findMax(f, max_f);
				}
			}
			// Get query and documents length
			double length = 0.0;
			for (String term : term_fs.keySet()) {
				if (Indexing.indexValue.containsKey(term)) {
					IndexStructure is = Indexing.indexValue.get(term);
					double tf = (double) term_fs.get(term) / max_f;
					double idf = Math.log(IndexStructure.getTotalDocsNum() / is.df) / Math.log(2);
					length += ((0.5 + 0.5 * tf) * idf) * ((0.5 + 0.5 * tf) * idf);
				}
			}
			length = Math.sqrt(length);
			Indexing.calculateDocumentsLength();
			// Calculate similarity
			queryResult[i].tweet_sims = new HashMap<String, Double>();
			for (String term : term_fs.keySet()) {
				if (Indexing.indexValue.containsKey(term)) {
					IndexStructure is = Indexing.indexValue.get(term);
					double tf_1 = (double) term_fs.get(term) / max_f;
					double idf = Math.log(IndexStructure.getTotalDocsNum() / is.df) / Math.log(2);
					for (String tweetID : is.tweetID_fs.keySet()) {
						double tf_2 = (double) is.tweetID_fs.get(tweetID) / Indexing.tweet_maxfs.get(tweetID);
						double sim;
						if (!queryResult[i].tweet_sims.containsKey(tweetID)) {
							sim = ((0.5 + 0.5 * tf_1) * idf) * (tf_2 * idf) / (length * Indexing.tweet_length.get(tweetID));
							queryResult[i].tweet_sims.put(tweetID, sim);
						} else {
							sim = queryResult[i].tweet_sims.get(tweetID);
							sim += ((0.5 + 0.5 * tf_1) * idf) * (tf_2 * idf) / (length * Indexing.tweet_length.get(tweetID));
							queryResult[i].tweet_sims.replace(tweetID, sim);
						}
					}
				}
			}
		}
		printQueryResult();
	}
	
	private static void printQueryResult() {
		File outputFile = new File(Main.QUERY_RESULT_FILE_PATH);
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			String queryID;
			ArrayList<Result> result_array;
			for (int i = 0; i < QUERY_NUM; i++) {
				queryID = queryResult[i].querID;
			
				result_array = new ArrayList<Result>();
				for (String tweetID : queryResult[i].tweet_sims.keySet()) {
					result_array.add(new Result(tweetID, queryResult[i].tweet_sims.get(tweetID)));
				}
			
				// Sort
				Collections.sort(result_array, new Comparator<Result>() {
					public int compare(Result o1, Result o2) {
						return -Double.compare(o1.similarity, o2.similarity);
					}
				});
				
				// Write
				String output = null;
				for (int j = 0; j < result_array.size() && j < 1000; j++) {
					output = queryID + " Q0 ";
					output += result_array.get(j).tweetID + " ";
					output += String.valueOf(j + 1) + " ";
					output += String.valueOf(result_array.get(j).similarity) + " ";
					output += "Jiayao&Zhiyan";
					bw.write(output);
					bw.newLine();
				}
			}
			
			bw.close();
		} catch (IOException e) {
			System.out.println("Error occurs during writing query result file:" + e.getMessage());
		}
	}
	
	public static void printPreprocessedQuery() {
		File outputFile = new File(Main.PREPROCESSED_QUERY_FILE_PATH);
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			for (int i = 0; i < output.length; i++) {
				bw.write(output[i]);
				bw.newLine();
			}
			
			bw.close();
		} catch (IOException e) {
			System.out.println("Error occurs during writing preprocessed query file:" + e.getMessage());
		}
	}
	
	private static String preprocessQuery(String input) {
		String output = "";
		
		Preprocessing.loadStopWords(Main.STOPWORDS_FILE_PATH);
		
		// Filtering out punctuation tokens
		input = input.replaceAll("\\W", " ");
		input = input.replaceAll("\\s+", " ");
		
		String[] t = input.split(" ");
		// Filtering out numbers and stopwords
		for (int i = 0; i < t.length; i++) {
			if (Preprocessing.isWord(t[i])) {
				// Filtering out stopwords
				if (!Preprocessing.stopWords.containsKey(t[i])) {
					// Stemming
					char[] w = t[i].toCharArray();
					Stemmer s = new Stemmer();
					s.add(w, w.length);
					s.stem();
					output += s.toString() + " ";
				}
			}
		}
		// Clear the last space
		output = output.substring(0, output.length() - 1);
		
		return output;
	}
	
}
