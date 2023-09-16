package textprocessing.textgeneration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.aspose.email.MailAddress;
import com.aspose.email.MailAddressCollection;
import com.aspose.email.MailMessage;

import ache.model.ScientificEvent;

public class TemplateFetcher {



	public static String getAppliedTemplate(String emailTemplate, Map<String, String> input) {
		List<String> templatesToUpdate = Arrays.asList(emailTemplate.split("<<"));
		templatesToUpdate = templatesToUpdate.subList(1, templatesToUpdate.size());
		templatesToUpdate = templatesToUpdate.stream().map(x->x.substring(0, x.indexOf(">>"))).collect(Collectors.toList());
		
		String res = emailTemplate;
		for(String s:templatesToUpdate)
		{
			if(!input.containsKey(s))
				throw new Error("Input does not contain:"+s+" for "+input);
			res = res.replaceAll("<<"+s+">>", input.get(s));
		}
		
		return res;
	}
	

}
