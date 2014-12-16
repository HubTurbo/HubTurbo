package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
	
	public static String snakeCaseToCamelCase(String str) {
        Pattern p = Pattern.compile("(^|_)([a-z])" );
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(2).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
	}

}
