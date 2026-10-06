package com.example.contacts.data;

public class ContactModel {
    public long id;
    public long rawContactId;
    public String name;
    public String number;
    public String type;
    public String photoUri;
    public boolean favorite;
    public String t9Name;

    public ContactModel(long id, long rawContactId, String name, String number,
                        String type, String photoUri, boolean favorite) {
        this.id = id;
        this.rawContactId = rawContactId;
        this.name = name == null ? "" : name;
        this.number = number == null ? "" : number;
        this.type = type == null ? "" : type;
        this.photoUri = photoUri;
        this.favorite = favorite;
        this.t9Name = "";
    }
}
