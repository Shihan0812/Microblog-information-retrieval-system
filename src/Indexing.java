import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;


public class Indexing {
	
	public static HashMap<String, IndexStructure> indexValue = new HashMap<String, IndexStructure>();
	// Hold the maximum frequency of terms in each document
	public static HashMap<String, Integer> tweet_maxfs = new HashMap<String, Integer>();
	// Hold the lenght of each document
	public static HashMap<String, Double> tweet_length = new HashMap<String, Double>();
	
	public static void index() {
		try {
			FileReader fr = new FileReader(Main.PREPROCESSED_DOC_FILE_PATH);
			BufferedReader br = new BufferedReader(fr);

			// For each document
			String temp;
			while ((temp = br.readLine()) != null) {
				int max_f = 0;
				String[] t = temp.split(" ");
				// Get the tweet id
				String tweetID = t[0];
				// Renew the index for each term
				for (int i = 1; i < t.length; i++) {
					// If the term is not in the index
					if (!indexValue.containsKey(t[i])) {
						IndexStructure is = new IndexStructure();
						is.df = 1;
						is.tweetID_fs = new HashMap<String, Integer>();
						is.tweetID_fs.put(tweetID, 1);
						indexValue.put(t[i], is);
						
						max_f = findMax(1, max_f);
					} else {
						IndexStructure is = indexValue.get(t[i]);
						// If the document has not been added
						if (!is.tweetID_fs.containsKey(tweetID)) {
							is.df++;
							is.tweetID_fs.put(tweetID, 1);
							
							max_f = findMax(1, max_f);
						} else {
							int f = is.tweetID_fs.get(tweetID);
							f++;
							is.tweetID_fs.replace(tweetID, f);
							
							max_f = findMax(f, max_f);
						}
					}
				}
				// Set the maximum frequency of terms in the document
				tweet_maxfs.put(tweetID, max_f);
				// Increase total document number
				IndexStructure.increaseTotalDocsNum();
			}
			
			br.close();
		} catch (IOException e) {
			System.out.println("Error occurs during indexing:" + e.getMessage());
		}
	}
	
	public static void calculateDocumentsLength() {
		try {
			FileReader fr = new FileReader(Main.PREPROCESSED_DOC_FILE_PATH);
			BufferedReader br = new BufferedReader(fr);

			// For each document
			HashMap<String, Integer> appearedTerms;
			String temp;
			while ((temp = br.readLine()) != null) {
				double length = 0.0;
				appearedTerms = new HashMap<String, Integer>();
				
				String[] t = temp.split(" ");
				// Get the tweet id
				String tweetID = t[0];
				for (int i = 1; i < t.length; i++) {
					if (!appearedTerms.containsKey(t[i])) {
						IndexStructure is = indexValue.get(t[i]);
						double idf = Math.log(IndexStructure.getTotalDocsNum() / is.df) / Math.log(2);
						double tf = (double) is.tweetID_fs.get(tweetID) / tweet_maxfs.get(tweetID);
						length += (tf * idf) * (tf * idf);
						appearedTerms.put(t[i], 0);
					}
				}
				length = Math.sqrt(length);
				tweet_length.put(tweetID, length);
			}
			
			br.close();
		} catch (IOException e) {
			System.out.println("Error occurs during calculating lengths:" + e.getMessage());
		}
	}
	
	public static int findMax(int a, int b) {
		return a > b? a : b;
	}
	
	public static void printIndex() {
		File outputFile = new File(Main.INDEX_FILE_PATH);
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			for (String term : indexValue.keySet()) {
				IndexStructure is = indexValue.get(term);
				
				// Term
				String output = term + ", ";
				// Number of documents containing the term
				output += String.valueOf(is.df) + ": ";
				
				for (String tweetID : is.tweetID_fs.keySet()) {
					output += tweetID + ": ";
					output += String.valueOf(is.tweetID_fs.get(tweetID)) + ", ";
				}
				bw.write(output);
				bw.newLine();
			}
			
			bw.close();
		} catch (IOException e) {
			System.out.println("Error occurs during writing index file:" + e.getMessage());
		}
	}
	
}
