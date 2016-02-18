package driver;

import modules.Word2Vec;

public class Driver {

	
	
	public static void main(String[] args) {
		Word2Vec w2 = new Word2Vec();
//		System.out.println(w2.printStopWords());
		w2.loadTrainingData("data/labeledTrainData.tsv", 1);
		System.out.println(w2.getVectorElement("review", 12));
	}

}
