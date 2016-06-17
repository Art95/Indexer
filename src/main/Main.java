package main;

import indexer.Indexer;

import java.io.File;

/**
 * Created by artem on 12.06.16.
 */
public class Main {
    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        File dir = new File("/home/artem/Documents/GMO corpus/Test/");
        indexer.index(dir);
        indexer = new Indexer();
    }
}
