package com.example.contacts.data;
import java.util.ArrayList;
public class ContactModel{
 public static class PhoneNumber{public String number,type;public PhoneNumber(String n,String t){number=n==null?"":n;type=t==null?"טלפון":t;}}
 public long id;public String name="";public String photoUri="";public boolean favorite;public String t9Name="";
 public final ArrayList<PhoneNumber> phones=new ArrayList<PhoneNumber>();
 public ContactModel(long id,String name){this.id=id;this.name=name==null?"":name;}
 public String primary(){return phones.size()==0?"":phones.get(0).number;}
}