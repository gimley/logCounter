package org.tumblr;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thor on 5/4/16.
 */
public enum LogCounter {
    LOG_COUNTER; // Singleton class

    private static Map<Integer, Multiset<Long>> statusMap= new ConcurrentHashMap<>();


    /*
       @param l: date represented as long yyyyMMddHHmm
       @param statusCode: HTTP status code from access log
     */
    public void increment(Long dt, Integer statusCode) {
        statusMap.putIfAbsent(statusCode, ConcurrentHashMultiset.create());
        statusMap.get(statusCode).add(dt);
    }

    /*
        Dumps result to output.csv
     */
    public void printResult(String outputFilename) throws IOException{
        List<Integer> sortedKeys = new ArrayList<>(statusMap.size());
        sortedKeys.addAll(statusMap.keySet());
        Collections.sort(sortedKeys);
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm");
        outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        FileWriter fileWriter = new FileWriter(outputFilename);

        for (Integer statusCode : sortedKeys) {
            List<Long> sortedDateKeys = new ArrayList<>(statusMap.size());

            Multiset<Long> countMap = statusMap.get(statusCode);
            sortedDateKeys.addAll(countMap.elementSet());
            Collections.sort(sortedDateKeys);

            fileWriter.write("# time, " + statusCode + "\n");
            for (Long l : sortedDateKeys) {
                try {
                    fileWriter.write(outputDateFormat.format(inputDateFormat.parse(l.toString())) + ", " + countMap.count(l) + "\n");
                } catch (ParseException e) {
                    fileWriter.close();
                    e.printStackTrace();
                    return;
                }
            }
        }
        fileWriter.close();
    }
}