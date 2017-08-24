import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class Main {
	
	// Input files path
	public final static String INPUT_PATH = "src/Input Files/";
	public final static String INPUT_DOCUMENT = INPUT_PATH + "Trec_microblog11.txt";
	public final static String STOPWORDS_FILE_PATH = INPUT_PATH + "StopWords.txt";
	public final static String QUERY_FILE_PATH = INPUT_PATH + "topics_MB1-50.txt";
	// Output files path
	public final static String OUTPUT_PATH = "src/Output Files/";
	public final static String PREPROCESSED_DOC_FILE_PATH = OUTPUT_PATH + "Preprocessed Documents.txt";
	public final static String INDEX_FILE_PATH = OUTPUT_PATH + "Index.txt";
	public final static String PREPROCESSED_QUERY_FILE_PATH = OUTPUT_PATH + "Preprocessed Query.txt";
	public final static String QUERY_RESULT_FILE_PATH = OUTPUT_PATH + "Results.txt";
	public final static String PREPROCESSED_EVALUATE_FILE = OUTPUT_PATH + "Modified_Results.txt";
	
	public static void main(String[] args) {
		System.out.println("************ Microblog Information Retrieval System ************");
		
		System.out.println("\nPreprocessing the documents...");
		Preprocessing.preprocess();
		System.out.println("Finish preprocessing! File saved in " + PREPROCESSED_DOC_FILE_PATH);
		
		System.out.println("\nIndexing...");
		Indexing.index();
		System.out.println("Finish Indexing!");
		
		System.out.println("\nPreprocessing the querys...");
		Query.readQueryFile();
		System.out.println("Finish preprocessing querys!");
		
		System.out.println("\nRetrieval and Ranking...");
		Query.modified_query();
		System.out.println("Finished! Results file save in " + QUERY_RESULT_FILE_PATH);
		
		System.out.println("************ Microblog Information Retrieval System ************");
		System.out.println("Input 1. Print the index in txt file");
		System.out.println("Input 2. Print the preprocessed querys");
		System.out.println("Input 3. Remove 'MB0' in query num of Results file for evaluation");
		System.out.println("****************************************************************");
		BufferedReader bufferRead;
		boolean one = true, two = true, three = true;
		while (one || two || three) {
			try{
				bufferRead = new BufferedReader(new InputStreamReader(System.in));
				String s = bufferRead.readLine();
				
				if (s.equals("1")) {
					System.out.println("Printing Index...");
					Indexing.printIndex();
					System.out.println("Finished! File save in " + INDEX_FILE_PATH);
					one = false;
				} else if (s.equals("2")) {
					System.out.println("Printing preprocessed querys...");
					Query.printPreprocessedQuery();
					System.out.println("Finished! File save in " + PREPROCESSED_QUERY_FILE_PATH);
					two = false;
				}else if (s.equals("3")) {
					System.out.println("Printing modified results...");
					preprocessResults();
					System.out.println("Finished! File save in " + PREPROCESSED_EVALUATE_FILE);
					three = false;
				} else {
					System.out.println("Invalid input!");
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("************ Microblog Information Retrieval System ************");
		System.out.println("All processing finished!");
		System.out.println("****************************************************************");
	}
	
	// Removing MB0 to results' query num so that it can match the expected results
	private static void preprocessResults() {
		File outputFile = new File(Main.PREPROCESSED_EVALUATE_FILE);
		try {
			FileReader fr = new FileReader(Main.QUERY_RESULT_FILE_PATH);
			BufferedReader br = new BufferedReader(fr);
			
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			String temp;
			while ((temp = br.readLine()) != null) {
				temp = temp.replaceFirst("MB[0]+", "");
				bw.write(temp);
				bw.newLine();
			}
			
			br.close();
			bw.close();
		} catch (IOException e) {
			System.out.println("Error occurs during preprocessing:" + e.getMessage());
		}
	}
}
