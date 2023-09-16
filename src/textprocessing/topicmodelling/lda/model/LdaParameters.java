package textprocessing.topicmodelling.lda.model;

import java.util.Arrays;

public class LdaParameters {
	
	private final int nbOfTopics;
	private final String alpha;
	private final String beta;
	private final int passes;
	
	public LdaParameters(int nbOfTopics, String alpha, String beta, int passes) {
		if(nbOfTopics<=1) throw new Error();
		this.nbOfTopics = nbOfTopics;
		this.alpha = alpha;
		this.beta = beta;
		this.passes = passes;
	}
	
	public static String toSerializableString(LdaParameters l) {
		return l.nbOfTopics+"\t"+l.alpha+"\t"+l.beta+"\t"+l.passes;
	}
	
	public static LdaParameters fromSerializableString(String s) {
		String[] str = s.split("\t");
		return new LdaParameters(Integer.parseInt(str[0]),
				str[1], str[2], Integer.parseInt(str[3]));
	}

	public int getNbOfTopics() {
		return nbOfTopics;
	}

	public String getAlpha() {
		return alpha;
	}

	public String getBeta() {
		return beta;
	}

	public int getPasses() {
		return passes;
	}
	
	public String toString()
	{
		return "nb of topics:"+nbOfTopics+", alpha:"+alpha+", beta:"+beta+", passes:"+passes;
	}
	
	public int hashCode() {
		return nbOfTopics*alpha.hashCode()*beta.hashCode()*passes;
	}
	
	public boolean equals(Object o) {
		LdaParameters ldap = (LdaParameters)o;
		return (ldap.nbOfTopics==nbOfTopics) && ldap.alpha.equals(alpha) && ldap.beta.equals(beta) && ldap.passes == passes;
	}

}
