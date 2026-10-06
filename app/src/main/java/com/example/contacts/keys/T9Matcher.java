package com.example.contacts.keys;
import com.example.contacts.data.ContactModel;import com.example.contacts.util.PhoneFormatter;
public final class T9Matcher{
 private T9Matcher(){}
 private static final String[] EN={"","","abc","def","ghi","jkl","mno","pqrs","tuv","wxyz"};
 private static final String[] HE={"","","אבג","דהו","זחט","יכל","מנס","עפצ","קרש","ת"};
 public static String build(String s,boolean he){if(s==null)return "";String[] map=he?HE:EN;StringBuilder b=new StringBuilder();String low=s.toLowerCase();for(int i=0;i<low.length();i++){char ch=low.charAt(i);int d=-1;for(int n=2;n<=9;n++)if(map[n].indexOf(ch)>=0){d=n;break;}b.append(d<0?' ':('0'+d));}return b.toString().trim();}
 public static boolean matches(ContactModel c,String q,boolean he){if(q==null||q.length()==0)return true;String d=q.replaceAll("[^0-9]","");if(PhoneFormatter.normalize(c.primary()).contains(d)||PhoneFormatter.last4(c.primary()).contains(d))return true;String t=build(c.name,he).replace(" ","");if(t.contains(d))return true;String[] w=c.name.trim().split("\\s+");for(String x:w)if(build(x,he).replace(" ","").startsWith(d))return true;return false;}
}