package com.example.contacts.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public final class ContactsRepository {
    private ContactsRepository() {}

    public static Cursor createPhoneCursor(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] p = new String[] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        };
        return cr.query(uri, p, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    public static List<ContactModel> fromCursor(Cursor c) {
        List<ContactModel> out = new ArrayList<ContactModel>();
        if (c == null) return out;
        long lastId = Long.MIN_VALUE;
        while (c.moveToNext()) {
            long id = c.getLong(1);
            if (id == lastId) continue;
            lastId = id;
            String name = c.getString(2);
            String number = c.getString(3);
            int typeCode = c.getInt(4);
            String type = typeLabel(typeCode);
            String photo = c.getString(5);
            boolean star = c.getInt(6) != 0;
            out.add(new ContactModel(id, -1L, name, number, type, photo, star));
        }
        return out;
    }

    private static String typeLabel(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return "נייד";
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return "בית";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return "עבודה";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN: return "ראשי";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME: return "פקס בית";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK: return "פקס עבודה";
            default: return "טלפון";
        }
    }

    public static String lookupName(Context ctx, String number) {
        if (number == null || number.length() == 0) return "";
        Cursor c = null;
        try {
            Uri u = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            c = ctx.getContentResolver().query(u,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                    null, null, null);
            if (c != null && c.moveToFirst()) return c.getString(0);
        } finally {
            if (c != null) c.close();
        }
        return "";
    }

    public static long findRawContact(Context ctx, long contactId) {
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts._ID},
                    ContactsContract.RawContacts.CONTACT_ID + "=?",
                    new String[]{String.valueOf(contactId)}, null);
            if (c != null && c.moveToFirst()) return c.getLong(0);
        } finally {
            if (c != null) c.close();
        }
        return -1L;
    }
}
