package textprocessing.languagemodels;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnglishDictionnary {
	private static final Set<String> ENGLISH_DICTIONNARY=new HashSet<>();
	private static final File DATABASE_ENGLISH = Paths.get("data/english_words.txt").toFile();

	static {		
		try {
			/*	int index = 0;
			for(String s: Files.readAllLines(DATABASE_ENGLISH.toPath()))
			{
				index++;
				System.out.println(index+" "+s);
				ENGLISH_DICTIONNARY.add(s);
				if(s.equals("ny"))
					System.out.println();
			}*/


			ENGLISH_DICTIONNARY.addAll(Files.readAllLines(DATABASE_ENGLISH.toPath())
					.parallelStream().collect(Collectors.toSet()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static final boolean contains(String s) { return ENGLISH_DICTIONNARY.contains(s);}
}
