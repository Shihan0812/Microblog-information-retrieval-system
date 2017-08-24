import java.util.HashMap;


public class IndexStructure {
	// Total number of Documents
	private static int totalDocsNum = 0;
	
	// Number of documents containing the term
	public int df;
	// Tweet ID and frequency of the term in that tweet
	public HashMap<String, Integer> tweetID_fs;
	
	
	public static void increaseTotalDocsNum() {
		totalDocsNum++;
	}
	
	public static int getTotalDocsNum() {
		return totalDocsNum;
	}
}
