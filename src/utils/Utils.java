package src.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Utils {
    public static Date convertStringToDate(String date) {
        if (date == null) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String concatWithCommas(ArrayList<Integer> items) {
        StringBuilder wordList = new StringBuilder();
        for (Integer word : items) {
            wordList.append(word + ",");
        }
        return new String(wordList.deleteCharAt(wordList.length() - 1));
    }
}
