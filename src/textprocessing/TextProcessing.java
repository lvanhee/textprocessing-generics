package textprocessing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextProcessing {
	
	public static String filterRawString(String s, boolean onlyImportantWords)
	{
		s = s.replaceAll("\\.", " . ").replaceAll("�", " ").replaceAll("\\?", "").replaceAll("�", "").replaceAll("�","").
				replaceAll("!","").
				replaceAll("�","");
        
        if(onlyImportantWords)
        {
        	s = s.replaceAll("\\.","");
        	s = s.replaceAll("\\(","");
        	s = s.toLowerCase();
        	List<String> uselessWords = Arrays.asList(".", "de","et","la", "les","�", "l", "est", "d", "que", "des", "le", "un", "en",
        			"nous", "une", "qui", "pas", "ne", "qu", "pour", "dans","ce", "plus", "n","sont", "il", "du", "on",
        			"vous", "ou", "se", "par", "mais", "au", "notre", "sur", "si", "tout", "ils", "je", "m�me",
        			"comme", "elle", "leur", "avec", "cette", "cela", "fait", "ces", "�tre", "son", "aussi", "ont",
        			"nos", "donc","autres","via", "peu", "car","tous", "�a", "aux", "avons", "tr�s", "sommes", "faire",
        			"a", "sans", ".", "peut", "sa", "ses","ai", "autre", "toujours", "certains", "�tait",
        			"non", "o�", "leurs", "tu", "lui", "votre", "encore", "toutes", "entre", "avoir", "quelques", "rien",
        			"avant", "jamais","avait", "me", "soit", "ceux", "l�", "moins", "peuvent", "dire", "toute","�t�",
        			"elles","lors","possible", "trop", "cas", "assez", "fois", "grande", "afin", "eux", "ainsi","d�s",
        			"vers","vraiment", "apr�s","chose", "tant", "alors", "chez", "dit", "reste","ailleurs", "ni","oui",
        			"pouvons","sera","souvent", "deux","etc","sous","voir","beaucoup","certaines","quand","bon","dont",
        			"doute","mes","nom","quel", "ton","utiliser", "veut","cet", "jusqu", "lorsqu","vos","comment",
        			"contre","faut","doit","existe","ici","sais", "seul","vu","aucun","eu","font", "pu","celui","mani�re",
        			"as","aura","avaient","jours","parler","suis","fut","ma","moi","mon","objet","quant","quelque",
        			"agit","ayant","loin","lorsque","mis","pr�s","selon","sait","sortes","vite","ou","plut�t","quoi",
        			"sein","semble","plupart","�tes","ceci","g�n�ralement","entendu","d�j�","autant","celle",
        			"extr�mement","n�anmoins", "pourrait","quelle", "sinon","sortir","vue","�tant", "�tat",
        			"aurait", "bref", "celle-ci","essentiellement","exemple", "furent","m�mes","prendre","puis",
        			"te", "durant", "devez","soient", "ait","utilis�","voit","celles", "savons", "celles", "met","allait", "avions",
        			"droite", "vivant","sert","certes", "pouvoir", "�me", "devoir", "savoir", "pourtant", "chaque","\\�", "�","�", "fasse",
        			"pr�f�rable", "pouvez", "pouvaient", "pouvait", "devrez", "devrait", "devrons", "dois", "permet", "permettre",
        			"permettrait", "permettant","permettra", "permettent", "permettaient", "permis", "mettre", "rendre", "rendent",
        			"rendu", "aider", "faisait", "faisons", "faisant", "fait", "refaisait", "comprend", "comprends", "comprendras",
        			"comprenais", "appeler", "appellent", "appelle", "appela", "appelaient","appellera", "seul", "seule","viennent",
        			"vient", "venir", "second", "seconds", "secondes", "seconde", "21�me", "jour", "terme","pense", "quelqu", "truc",
        			"trucs", "pose", "rester", "restera", "donn�","puissions", "coup", "put", "choses", "pensent","finalement",
        			"parfois", "simplement", "g�n�rales", "fort", "presque", "grand", "grandes","veulent", "veuille","d�",
        			"vois", "voies", "derniers", "assurer","base","petit", "petits", "�galement", "niveau","derni�re","nombre","puisse",
        			"devait","rappelle","donnera", "donne","sens","sorte","uniquement","juste","simple","pourrions", "point", "important",
        			"importante"
        			
        			);
        	for(String useless: uselessWords)
        		s = s.replaceAll(" "+useless+" ", " ");
        		
        }
        
        while(s.contains("  "))s = s.replaceAll("  ", " ");

        return s;
	}

	public static void printNGram(Map<List<String>, Integer> allNGrams) {
		List<List<String>> sorted = allNGrams.keySet().stream().collect(Collectors.toList());
		
		Collections.sort(sorted, (x,y)->{
			if(allNGrams.get(x)>allNGrams.get(y))return -1;
			if(allNGrams.get(x)<allNGrams.get(y))return 1;
			return x.toString().compareTo(y.toString());
		});
		
		for(List<String> l :sorted)
			System.out.println(l+":"+allNGrams.get(l));
		
	}

}
