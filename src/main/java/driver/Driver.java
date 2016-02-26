package driver;

import modules.Word2Vec;

public class Driver {

	
	
	public static void main(String[] args) {
		Word2Vec w2 = new Word2Vec();
//		System.out.println(w2.printStopWords());
		w2.preProcessTrainingDataFromCSVTSV("data/labeledTrainData.tsv", 1);
		//w2.writePreprocessedTrainingData("trainingData.txt");
		w2.trainModel(); //add parameters needed
//		w2.printVocab(10);
		w2.InitNet(100); //initiate neural net
//		w2.writeNeuralNet();
		w2.printCode4EachVocab(10);
		System.out.println("Size of vocab:\t" + w2.getSizeOfVocab());
		//System.out.println(w2.getVectorElement("review", 12));
	}

}
