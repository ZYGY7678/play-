package com.example.contacts.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.example.contacts.util.PhoneFormatter;

import java.util.ArrayList;
import java.util.List;

public final class CallLogRepository {
    private CallLogRepository() {}

    public static class Entry {
        public String number;
        public String name;
        public int type;
        public long date;
        public int count;
        public boolean known;
        public Entry(String number, String name, int type, long date) {
            this.number = number == null ? "" : number;
            this.name = name == null ? "" : name;
            this.type = type;
            this.date = date;
            this.count = 1;
            this.known = this.name.length() > 0;
        }
    }

    public static List<Entry> load(Context ctx) {
        List<Entry> out = new ArrayList<Entry>();
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.TYPE, CallLog.Calls.DATE},
                    null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            if (c == null) return out;
            Entry last = null;
            while (c.moveToNext()) {
                String number = c.getString(0);
                String name = c.getString(1);
                int type = c.getInt(2);
                long date = c.getLong(3);
                if (last != null && PhoneFormatter.sameNumber(last.number, number)) {
                    last.count++;
                } else {
                    last = new Entry(number, name, type, date);
                    if (!last.known) {
                        last.name = number;
                    }
                    out.add(last);
                }
                if (out.size() >= 400) break;
            }
        } finally {
            if (c != null) c.close();
        }
        return out;
    }
}
