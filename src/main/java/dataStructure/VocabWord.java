package dataStructure;

import misc.Cons;

public class VocabWord {
	public Long count;
	public int[] point;
	public String word;
	public String code;
	public String codelen;
	
	public VocabWord(){
		count = new Long(1);
		point = new int[Cons.MAX_CODE_LENGTH];
	}
}
