package modules;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;

import misc.Cons;

public class Word2Vec {
	
	private ArrayList<String[]> dataVectors;
	private ArrayList<String> stopwords;
	private HashMap<String, Integer> labelIndexMap;
	private ArrayList<String> targetLabels;
	
	public Word2Vec(){
		labelIndexMap = new HashMap<String, Integer>();
		stopwords = new ArrayList<String>();
		targetLabels = new ArrayList<String>();
		loadConfiguration();
		loadStopWords();
		System.out.println(printTargetLabels());
		System.out.println(printStopWords());
		
	}
	
	
	/**
	 * This methods load training data written in csv and tsv only
	 * @param filePath
	 */
	public void loadTrainingData(String trainingFilePath, int cleanOption){
		try {
			FileInputStream fis = new FileInputStream(trainingFilePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
			String delimiter = null;
			System.out.println("Read " + trainingFilePath + " successfully.");
			//decide delimiter according to file type
			if(trainingFilePath.toLowerCase().endsWith(".csv"))
				delimiter = Cons.COMMA;
			else if(trainingFilePath.toLowerCase().endsWith(".tsv"))
				delimiter = Cons.TAB;
			
			String line = "";
			int lineCount = 0;
			while( (line = br.readLine()) != null ){
				String[] tokens = line.split(delimiter);
				if(lineCount == 0){
					dataVectors = new ArrayList<String[]>(tokens.length);
					for(int clmnCount = 0; clmnCount < tokens.length; clmnCount++){
						labelIndexMap.put(getRidofQuotes(tokens[clmnCount].toLowerCase()), clmnCount);
					}
					System.out.println("Labels:\t" + labelIndexMap.keySet().toString());
					System.out.println("Number of Columns:\t" + dataVectors.size());
				} else{
					Iterator<String> itr = labelIndexMap.keySet().iterator();
					
					for(int clmnCount = 0; clmnCount < tokens.length; clmnCount++){
						//DO preprocessing here
						String data = getRidofQuotes(tokens[clmnCount]);
						
//						dataVectors.get(clmnCount).add(getRidofQuotes(tokens[clmnCount]));
					}
				}
				lineCount++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//cleans data if option is on
		if(cleanOption == 1){
			for(int i = 0; i < targetLabels.size(); i++){
				cleanData(targetLabels.get(i));
			}
			System.out.println("Done cleaning HTML tag.");
		}
	}
	
	/**
	 * returns the elements in a specific vector
	 * @param label
	 * @return
	 */
	public String getVectorElement(String label){
		return dataVectors.get(labelIndexMap.get(label)).toString();
	}
	
	/**
	 * returns the elements at the index of a specific vector
	 * @param label
	 * @param index
	 * @return
	 */
	public String getVectorElement(String label, int index){
		String result = "";
		try{
//			result = dataVectors.get(labelIndexMap.get(label)).get(index);
		} catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public String printStopWords(){
		return stopwords.toString();
	}
	
	public String printTargetLabels(){
		return targetLabels.toString();
	}
	
	private void loadConfiguration(){
		try{
			FileInputStream fis = new FileInputStream("config/config");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
			String line = "";
			while( (line = br.readLine()) != null){
				line = line.trim();
				if(!line.startsWith(Cons.SHARP)){
					String[] tokens = line.split("=");
					if(tokens.length > 0){
						if(tokens[0].equals("target_label")){
							String params[] = tokens[1].split(Cons.COMMA);
							for(int i = 0; i < params.length; i++){
								targetLabels.add(params[i]);
							}
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch( IOException e){
			
		}
	}
	
	private void loadStopWords(){
		try {
			FileInputStream fis = new FileInputStream("config/stopwords");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
			String line = "";
			while( (line = br.readLine()) != null){
				stopwords.add(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns the word of string type after geting rid of either single quote or double quote
	 * @param word
	 * @return
	 */
	private String getRidofQuotes(String word){
		if( (word.startsWith("\"") || word.startsWith("\'") ) 
				&& ( word.endsWith("\"") || word.endsWith("\'")) ){
			word = word.substring(1);
			word = word.substring(0, word.length() - 1);
			return word;
		} else{
			return word;
		}
	}
	
	/**
	 * This method cleans up HTML and etc.
	 * and setting data for each label to lowercase letter, so IDs must be case-sensitive
	 * @param label
	 */
	private void cleanData(String label){
//		ArrayList<String> elements = dataVectors.get(labelIndexMap.get(label));
//		for(int i = 0; i < elements.size(); i++){
//			String data = elements.get(i);
//			data = html2text(data);   //remove html tag
//			data = HanguelEngOnly(data); //Leave Hanguel and English only
//			data = removeStopWords(data);
//			elements.set(i, data.toLowerCase());
//		}
	}
	
	/**
	 * remove stopwords from the string
	 * @param data
	 * @return
	 */
	private String removeStopWords(String data){
		
		return data;
	}
	
	/**
	 * removes all characters except Hanguel, English, and white space
	 * This method is called by cleanData method
	 * @param data
	 * @return
	 */
	private String HanguelEngOnly(String data){
		String pattern = "[^a-zA-Z가-힣 ]";
		data = data.replaceAll(pattern, " ");
		return data;
	}
	
	/**
	 * removes HTML tags
	 * Called by cleanData method
	 * @param html
	 * @return
	 */
	private String html2text(String html){
		return Jsoup.parse(html).text();
	}
}
