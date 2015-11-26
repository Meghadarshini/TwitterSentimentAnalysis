package pack;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class CategorizeTweet {
	private static String stopWordListFileName = "/home/megha/Desktop/stopwords.txt";
	private static List<String> stopWords = new ArrayList<String>();
	private static List<String> featureVector = new ArrayList<String>();
	
	private static Pattern p1 = Pattern.compile("(.)\1{1,}", Pattern.DOTALL);
	
	
	private static String tweet = "Apple #stock go up @@@hehe great";
	private final static String URL_REGEX = "((www\\.[\\s]+)|(https?://[^\\s]+))";
	private final static String CONSECUTIVE_CHARS = "([a-z])\\1{1,}";
	private final static String STARTS_WITH_NUMBER = "[1-9]\\s*(\\w+)";
	
	/*
	 List<String> tokenize(String tweet) {
			tweet = this.preprocess(tweet);
			return tokenize(tweet);
			}
*/
	
	public static String processTweet(String tweet) {
		tweet = tweet.trim().toLowerCase();
		tweet = tweet.replaceAll(URL_REGEX, "");  
		tweet = tweet.replaceAll(CONSECUTIVE_CHARS, "$1");
		tweet = tweet.replaceAll(STARTS_WITH_NUMBER, "");
		//remove username
		tweet = tweet.replaceAll("@([^\\s]+)", "");
		tweet = tweet.replaceAll("#", "");
		tweet = tweet.replaceAll(",", "");
		// escape html
		tweet = tweet.replaceAll("&amp;", "&");
		tweet = StringEscapeUtils.unescapeHtml4(tweet);
		/*
		 #trim
		    tweet = tweet.strip('\'"')
		    */
		return tweet;
	}
	
	
	
	public static String replaceTwoOrMoreLetters(String tweet) {
		//Pattern p1 = Pattern.compile("(.)\1{1,}", Pattern.DOTALL);
		
		tweet = CategorizeTweet.p1.matcher(tweet).replaceAll("");
		return tweet;
		
	}
	//get stopword 
	public static List<String> getStopWordList(String stopWordListFileName) {
		//stopWords.add("AT_USER");
		//stopWords.add("URL");
		
		try {
			FileReader fileReader = new FileReader(stopWordListFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);			
			String line;
			while((line = bufferedReader.readLine()) != null) {
			String word = line.trim();
			stopWords.add(word);
			line = bufferedReader.readLine();
			}
			/*
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stopWords;
	}
	
	//feature vector
	public static List<String> getFeatureVector(String tweet) {
		
		String words = tweet.trim();
		String[] wordList = words.split(" "); 
		for(String w: wordList) {
			w = replaceTwoOrMoreLetters(w);
			//pendng , remove "?" from tweet 
		//	w.replaceAll("?", "");
			w.replaceAll(".", "");
			boolean value = false;
			value = Character.isLetter(w.charAt(0));
			int found  = 0;
			for(String sw : stopWords) 
			{
				if( sw.matches(w)) {
					found = 1;
					
				}
					
			}
			if(value == false || found == 1) {
				
			}
			else {
				featureVector.add(w.toLowerCase());
			}
		}
		return featureVector;
	}
	
	//read tweets
	public static void read() throws IOException {
		String trainingData = "/home/megha/Desktop/sampleTweets.txt"; //csv file for later 
		//String trainingData = "/home/megha/trainingFile.csv";
		FileReader fileReader;
		try {
			fileReader = new FileReader(trainingData);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while((line = bufferedReader.readLine()) != null) {
				List<String>stopWords = getStopWordList(stopWordListFileName);
				String processedTweet = processTweet(line);
				featureVector = getFeatureVector(processedTweet);
				System.out.println(featureVector);
				//bufferedReader.close();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
	}
	
	
	//feature extraction 
	public static void featureExtraction() throws IOException {
		FileReader fileReader;
		try {
			fileReader = new FileReader("a.csv"); //input csv file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			List<String> tweets = new ArrayList<String>();
			String line;
			while((line = bufferedReader.readLine()) != null) {
				String[] lineArray = line.split(",");
				String tweet = lineArray[0];
				String sentiment = lineArray[1];
				String processedTweet;
				processedTweet = processTweet(tweet);
			//	featureVector = getFeatureVector(processedTweet, stopWords);
				//tweets.add(featureVector, sentiment);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void convertToArff() throws FileNotFoundException {
		// create base structure
		//create classes list 
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("positive");
		labels.add("negative");
		labels.add("neutral");
		
		//create attr list
		Attribute tweetAttribute = new Attribute("tweet", (ArrayList<String>) null);
		Attribute ratingAttribute = new Attribute("rating", labels);
		
		//create instance 
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(tweetAttribute);
		attributes.add(ratingAttribute);
		Instances trainingData = new Instances("twitterSentimentAnalysis", (ArrayList<Attribute>)attributes, 0);
		
		//set class index
		trainingData.setClassIndex(1);
		String filename = "/home/megha/Desktop/full-corpus.csv";
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		String line;
		try {
			while((line = bufferedReader.readLine()) != null) {
				String[] tweetsplit = line.split("\t");
				System.out.println(tweetsplit.length);
			//	if(tweetsplit.length <6 || tweetsplit[5].isEmpty()) 
				//	continue;
				
				double[] data = new double[trainingData.numAttributes()];
				data[0] = new Double(tweetsplit[0]) - 1;
				//data[0] = traindngData.attribute(0).addStringValue(tweetsplit[2]); 
				//data[1] = new Double(tweetsplit[5]) - 1;
				
				Instance tweet = new DenseInstance(1.0, data);
				trainingData.add(tweet);	
				
				//save instances to arff file
				if(!trainingData.isEmpty()) {
					ArffSaver arffSaver = new ArffSaver();
					arffSaver.setFile(new File("/home/megha/testsetlatestWekaJava.arff"));
					//arffSaver.setFile(new File("/home/megha/tweetArffFileWeka.arff"));
					arffSaver.setInstances(trainingData);
					arffSaver.writeBatch();
				}				
				
			}
			bufferedReader.close();			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		

		
		
		
		
	}
	
	
	/*
	public void getBaseStructure() {

		
		
		
	}
	public static void incrementalClassifier() throws IOException, Exception {
		ArffLoader loader = new ArffLoader();
		try {
			loader.setFile(new File("m.arff"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Instances structure = loader.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);
		
		//train
		NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
		nb.buildClassifier(structure);
		Instance current;
		while((current = loader.getNextInstance(structure)) != null)
			nb.updateClassifier(current);
		System.out.println(nb);
	}
	*/
	
	public static void main(String[] args) throws IOException  {
		//String processedTweet = preprocess(tweet);
		//System.out.println(processedTweet);
		//CategorizeTweet();
		//read();
		convertToArff();
		

		
		
		
		
		
		
		

	}





}


