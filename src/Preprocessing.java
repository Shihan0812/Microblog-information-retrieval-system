import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;


public class Preprocessing {
	
	public static HashMap<String, Integer> stopWords = null;
	
	public static void preprocess() {
		loadStopWords(Main.STOPWORDS_FILE_PATH);
		
		File outputFile = new File(Main.PREPROCESSED_DOC_FILE_PATH);
		try {
			FileReader fr = new FileReader(Main.INPUT_DOCUMENT);
			BufferedReader br = new BufferedReader(fr);
			
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			String temp;
			boolean isFirstline = true; // Don't know why the first line start with a space
			while ((temp = br.readLine()) != null) {
				// Filtering out url and @user_name
				temp = temp.replaceAll("http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", " ");
				temp = temp.replaceAll("www.[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", " ");
				temp = temp.replaceAll("@[-a-zA-Z0-9_]*", " ");
				// Filtering out punctuation tokens
				temp = temp.replaceAll("\\W", " ");
				
				temp = temp.replaceAll("\\s+", " ");
				temp = temp.toLowerCase();
				if (isFirstline) {
					temp = temp.replaceFirst("\\s", "");
					isFirstline = false;
				}
				
				// Tokenization
				String[] t = temp.split(" ");
				// Get the tweet id
				String output = t[0] + " ";
				// Filtering out numbers and stopwords
				for (int i = 1; i < t.length; i++) {
					if (isWord(t[i])) {
						// Filtering out stopwords
						if (!stopWords.containsKey(t[i])) {
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
				bw.write(output);
				bw.newLine();
			}
			
			br.close();
			bw.close();
		} catch (IOException e) {
			System.out.println("Error occurs during preprocessing:" + e.getMessage());
		}
	}
	
	public static boolean isWord(String s) {
		return s.matches("[a-z]+");
	}
	
	public static void loadStopWords(String path) {
		if (stopWords == null) {
			stopWords = new HashMap<String, Integer>();
			try {
				FileReader fr = new FileReader(path);
				BufferedReader br = new BufferedReader(fr);
				
				String temp;
				while ((temp = br.readLine()) != null) {
					if (!stopWords.containsKey(temp)) {
						stopWords.put(temp, 0);
					}
				}
				
				br.close();
			} catch (IOException e) {
				System.out.println("Error occurs during preprocessing (StopWords):" + e.getMessage());
			}
		}
	}
	
}
