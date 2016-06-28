package main;

import indexer.Indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by artem on 12.06.16.
 */
public class Main {
    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        File dir = new File("./GMO corpus/Test/");
        indexer.index(dir);

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            try {
                String query = br.readLine();
                List<String> fileAddresses = indexer.findDocuments(query);
                System.out.println(fileAddresses);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
