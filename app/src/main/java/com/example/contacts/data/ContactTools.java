package com.example.contacts.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContactTools {
    private ContactTools() {}

    public static File exportVCard(Context c) throws Exception {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), "Download");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, "KeyContacts.vcf");
        Cursor x = null;
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), "UTF-8");
        try {
            x = c.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            long last = -1;
            if (x != null) while (x.moveToNext()) {
                long id = x.getLong(0);
                if (id == last) continue;
                last = id;
                String name = safe(x.getString(1));
                String number = safe(x.getString(2));
                w.write("BEGIN:VCARD\nVERSION:3.0\n");
                w.write("FN:" + name + "\nTEL:" + number + "\nEND:VCARD\n");
            }
        } finally {
            if (x != null) x.close();
            w.close();
        }
        return out;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ");
    }

    public static int importVCard(Context c, Uri uri) throws Exception {
        InputStream in = c.getContentResolver().openInputStream(uri);
        if (in == null) return 0;
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String name = "", phone = "";
        int count = 0;
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("FN:")) name = line.substring(3).trim();
            else if (line.startsWith("TEL")) {
                int k = line.indexOf(':');
                if (k >= 0) phone = line.substring(k + 1).trim();
            } else if (line.equals("END:VCARD")) {
                if (name.length() > 0 || phone.length() > 0) {
                    add(c, name, phone);
                    count++;
                }
                name = "";
                phone = "";
            }
        }
        r.close();
        return count;
    }

    private static void add(Context c, String name, String phone) {
        ContentValues raw = new ContentValues();
        Uri uri = c.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, raw);
        if (uri == null) return;
        long rawId = Long.parseLong(uri.getLastPathSegment());
        ContentValues n = new ContentValues();
        n.put(ContactsContract.Data.RAW_CONTACT_ID, rawId);
        n.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        n.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        c.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, n);
        if (phone.length() > 0) {
            ContentValues p = new ContentValues();
            p.put(ContactsContract.Data.RAW_CONTACT_ID, rawId);
            p.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            p.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            p.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            c.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, p);
        }
    }

    public static int mergeDuplicates(Context c) {
        Cursor x = null;
        Set<Long> remove = new HashSet<Long>();
        Map<String, Long> keep = new HashMap<String, Long>();
        try {
            x = c.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, null);
            if (x != null) while (x.moveToNext()) {
                long id = x.getLong(0);
                String n = PhoneFormatter.last9(x.getString(1));
                if (n.length() == 0) continue;
                if (!keep.containsKey(n)) keep.put(n, id);
                else if (keep.get(n).longValue() != id) remove.add(id);
            }
        } finally {
            if (x != null) x.close();
        }
        for (Long id : remove) c.getContentResolver().delete(
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.Contacts._ID + "=?",
                new String[]{String.valueOf(id)});
        return remove.size();
    }
}