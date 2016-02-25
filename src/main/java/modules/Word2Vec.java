package modules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import dataStructure.VocabWord;
import misc.Cons;

public class Word2Vec {
	
	
	
	private HashMap<String, ArrayList< ArrayList<String>>> dataMap;
	private ArrayList<String> stopwords;
	private HashMap<String, Integer> labelIndexMap;
	private HashMap<Integer, String> indexLabelMap;
	private ArrayList<String> targetLabels;
	private static Logger logger = Logger.getLogger(Word2Vec.class);
	private LinkedHashMap<String, VocabWord> vocab;    //[word, VocabWord]
	private double[] syn;
	private int inputLayerSize = -1;
	
	public Word2Vec(){
		
		labelIndexMap = new HashMap<String, Integer>();
		indexLabelMap = new HashMap<Integer, String>();
		stopwords = new ArrayList<String>();
		targetLabels = new ArrayList<String>();
		dataMap = new HashMap<String, ArrayList< ArrayList<String> >>();		
		loadConfiguration();
		loadStopWords();
		logger.info(printTargetLabels());
		logger.info(printStopWords());
	}
	
	
	
	/**
	 * #1. Train model
	 */
	public void trainModel(){
		vocab = new LinkedHashMap<String, VocabWord>(Cons.VOCAB_HASH_SIZE);
		Iterator<String> itr = dataMap.keySet().iterator();
		while(itr.hasNext()){
			String labelName = itr.next();
			ArrayList<ArrayList<String>> data = dataMap.get(labelName);
			while(data.size() > 0){
				ArrayList<String> tokens = data.get(0);
				for(int j = 0; j < tokens.size(); j++){
					String word = tokens.get(j).trim();
					if(!word.equals("") && word.length() < Cons.MAX_STRING){
						if(vocab.get(word) == null){
							VocabWord v = new VocabWord();
							vocab.put(word, v);
						}
						vocab.get(word).count++;
					}
				}
				data.remove(0);
			}
		}
		logger.info("vocab is created");
		logger.info("sorting vocab");
		Comparator<Entry<String, VocabWord>> valueComparator = (e1, e2) -> e1.getValue().count.compareTo(e2.getValue().count);
		vocab = vocab.entrySet().stream().sorted(Collections.reverseOrder(valueComparator))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		//beginning of word cut
		ArrayList<String> keys2beRemoved = new ArrayList<String>();
		
		//Reduce the size of vocab to 1/4 in case of movie review
		Iterator<String> keyItr = vocab.keySet().iterator();
		while(keyItr.hasNext()){
			String key = keyItr.next();
			if(vocab.get(key).count < Cons.VOCAB_THRESHOLD){
				keys2beRemoved.add(key);
			}
		}
		for(int i = 0; i < keys2beRemoved.size(); i++){
			vocab.remove(keys2beRemoved.get(i));
		}
		keys2beRemoved = null;   //I wonder if clean training data in a file is necessary.
		//end of word cut
		
	} //end of building vocab
	
	
	/**
	 * #2. Init Neural Network
	 */
	public void InitNet(int inputLayerSize){
		this.inputLayerSize = inputLayerSize;
		logger.info("Initiating Neural Network");
		long next_random = 1;
		syn = new double[vocab.size() * inputLayerSize];
		
		for(int i = 0; i < vocab.size(); i++){
			for(int j = 0; j < inputLayerSize; j++){
				next_random = next_random * (long)252149039 + 11;
				syn[i * inputLayerSize + j] = (((next_random & 0xFFFF) / (double)65536) - 0.5) / inputLayerSize;
			}
		}
		logger.info("Initiated input layer");
		createBinaryTree();
		//call createBinaryTree()
	}
	
	/**
	 * Create binary Huffman tree using the word counts
	 * Frequent words will have short unique binary codes
	 * 
	 * This method grants binary code for each vocab
	 */
	private void createBinaryTree(){
		long a, b, i, min1i, min2i, point[] = new long[Cons.MAX_CODE_LENGTH];
		long pos1 = vocab.size() - 1;
		long pos2 = vocab.size();
		String code;
		long count[] = new long[vocab.size() * 2 + 1];
		long binary[] = new long[vocab.size() * 2 + 1];
		long parent_node[] = new long[vocab.size() * 2 + 1];
		
		
		//Following algorithm constructs the Huffman tree by adding one node at a time
		for(a = 0; a < vocab.size() - 1; a++){
			//First, find two smallest nodes 'min1, min2'
			if(pos1 >= 0){
				if(count[(int)pos1] < count[(int)pos2]){
					min1i = pos1;
					pos1--;
				} else{
					min1i = pos2;
					pos2++;
				}
			} else{
				min1i = pos2;
				pos2++;
			}
			
			if(pos1 >= 0){
				if(count[(int)pos1] < count[(int)pos2]){
					min2i = pos1;
					pos1--;
				} else{
					min2i = pos2;
					pos2++;
				}
			} else{
				min2i = pos2;
				pos2++;
			}
			count[(int)(vocab.size() + a)] = count[(int)min1i] + count[(int)min2i];
			parent_node[(int)min1i] = vocab.size() + a;
			parent_node[(int)min2i] = vocab.size() + a;
			binary[(int)min2i] = 1;
		}
		
		//Now assign binary code to each vocabulary word
		for(a = 0; a < vocab.size(); a++){
			b = a;
			i = 0;
			while(true){
				code = binary[(int)b]+"";
				point[(int)i] = b;
				i++;
				b = parent_node[(int)b];
				if(b == vocab.size() * 2 -2)
					break;
			}
			
		}
	}
	
	
	/**
	 * Add features
	 * @param trainingFilePath
	 */
	public void LearnFromFormattedTrainingData(String trainingFilePath){
		//TODO
		//Implement logics
	}
	
	/**
	 * This methods load training data written in csv and tsv only
	 * @param filePath
	 */
	public void preProcessTrainingDataFromCSVTSV(String trainingFilePath, int cleanOption){
		logger.info("Reading Training Set");
		readTrainingSet(trainingFilePath, cleanOption);
		//cleans data if option is on
		if(cleanOption == 1){
			for(int i = 0; i < targetLabels.size(); i++){
				cleanData(targetLabels.get(i));
			}
			logger.info("Done cleaning HTML tag.");
		}
		
	}
	
	
	
	private void readTrainingSet(String trainingFilePath, int cleanOption){
		BufferedReader br = null;
		try{
			FileInputStream fis = new FileInputStream(trainingFilePath);
			br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
			String delimiter = null;
			if(br != null)
				logger.info("Read " + trainingFilePath + " successfully.");    //change it by logger later
			
			//decide delimiter according to file type
			if(trainingFilePath.toLowerCase().endsWith(".csv"))
				delimiter = Cons.COMMA;
			else if(trainingFilePath.toLowerCase().endsWith(".tsv"))
				delimiter = Cons.TAB;
			
			String line = "";
			int lineCount = 0;
			while( (line = br.readLine()) != null ){
				line = removeUTF8BOM(line);
				String[] tokens = line.split(delimiter);
				if(lineCount == 0){
					for(int clmnCount = 0; clmnCount < tokens.length; clmnCount++){ //Due to this section, label name can not be integer
						labelIndexMap.put(getRidofQuotes(tokens[clmnCount].toLowerCase()), clmnCount);
						indexLabelMap.put(clmnCount, getRidofQuotes(tokens[clmnCount].toLowerCase()));
						dataMap.put(indexLabelMap.get(clmnCount), new ArrayList< ArrayList<String>>());
					}
					logger.info("Labels:\t" + labelIndexMap.keySet().toString());
					logger.info("ColumnNums:\t" + indexLabelMap.keySet().toString());
				} else{
					for(int clmnCount = 0; clmnCount < tokens.length; clmnCount++){
						String label = indexLabelMap.get(clmnCount);
						if(targetLabels.contains(label)){
							String targetData[] = tokens[clmnCount].split("\\s+");
							ArrayList<String> targetData_list = new ArrayList<String>(targetData.length);
							for(int j = 0; j < targetData.length; j++){
								targetData_list.add(targetData[j].trim());
							}
							dataMap.get(indexLabelMap.get(clmnCount)).add(targetData_list);
						}
					}
				}
				lineCount++;
			}
			logger.info("dataMap_keySet:\t" + dataMap.keySet().toString());
			br.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	public void writePreprocessedTrainingData(String fileName){
		File targetFile = new File(fileName);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getPath(), false), "UTF8"));
			Iterator<String> itr = dataMap.keySet().iterator();
			while(itr.hasNext()){
				String labelName = itr.next();
				ArrayList<ArrayList<String>> data = dataMap.get(labelName);
				for(int i = 0; i < data.size(); i++){
					ArrayList<String> tokens = data.get(i);
					for(int j = 0; j < tokens.size(); j++){
						if(!tokens.get(j).trim().equals(""))
							bw.write(tokens.get(j).trim() + " ");
					}
					bw.newLine();
				}
				bw.flush();
			}
			bw.flush();
			bw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getSizeOfTrainingSet(){
		Iterator<String> itr = dataMap.keySet().iterator();
		int count = 0;
		while(itr.hasNext()){
			String labelName = itr.next();
			ArrayList<ArrayList<String>> data = dataMap.get(labelName);
			count += data.size();
		}
		return count;
	}
	
	public int getSizeOfVocab(){
		return vocab.keySet().size();
	}
	
	/**
	 * returns the elements in a specific vector
	 * @param label
	 * @return
	 */
	public String getVectorElement(String label){
		return "";
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
	
	/**
	 * Not a main function
	 * @return
	 */
	public String printStopWords(){
		return stopwords.toString();
	}
	
	/**
	 * Not a main function
	 * @return
	 */
	public String printTargetLabels(){
		return targetLabels.toString();
	}
	
	/**
	 * Not a main function
	 * @return
	 */
	public void printVocab(int num){
		Iterator<String> itr = vocab.keySet().iterator();
		while(itr.hasNext() && num > 0){
			String word = itr.next();
			System.out.println(num + "\t" + word + "\t" + vocab.get(word).count);
			num--;
		}
	}
	
	public void writeNeuralNet(){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("neuralNet.txt"));
			for(int i = 0; i < vocab.size(); i++){
				for(int j = 0; j < inputLayerSize; j++){
					bw.write(syn[i * inputLayerSize + j]+" ");
				}
				bw.newLine();
				bw.flush();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * 
	 * @param label
	 */
	private void cleanData(String label){
		ArrayList< ArrayList<String> > data = dataMap.get(label);
		for(int i = 0; i < data.size(); i++){
			ArrayList<String> tokens = data.get(i);
			for(int j = 0; j < tokens.size(); j++){
				String temp = html2text(tokens.get(j).trim());
				temp = HanguelEngOnly(temp);
				temp = removeStopWords(temp.trim());
				tokens.set(j, temp);
			}
		}
	}
	
	/**
	 * remove stopwords from the string
	 * @param data
	 * @return
	 */
	private String removeStopWords(String data){
		if(stopwords.contains(data.toLowerCase()))
			return "";
		else
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
		data = data.replaceAll(pattern, "");
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
	
	private static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        return s;
    }
}
