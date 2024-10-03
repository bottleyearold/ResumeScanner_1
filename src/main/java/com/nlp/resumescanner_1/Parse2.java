package com.nlp.resumescanner_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parse2 {
    public static void main (String args[]){
        String filepath = "/Users/yashutanneru/Downloads/resumescannertest.txt";
        List <String> keywords = new ArrayList <> ();
        keywords.add("Computer Science");
        keywords.add("Statistics");
        keywords.add("Ohio");

        try(BufferedReader a = new BufferedReader(new FileReader(filepath))){
            String line;
            while ((line = a.readLine()) != null){
                String lowerCaseLine = line.toLowerCase();
                for (String keyword : keywords){
                    String lowerCaseKeyword = keyword.toLowerCase();
                    if(lowerCaseLine.contains(lowerCaseKeyword)){
                        System.out.println("Keyword: " + keyword + " found in resume!");
                    }
                }
            }
        } catch (IOException e){
            System.err.println("Can't read file" + e.getMessage());
        }
    }
}
