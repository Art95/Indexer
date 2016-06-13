package main;

import indexer.DocumentAnalyzer;
import indexer.Indexer;
import searchengine.SearchEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by artem on 12.06.16.
 */
public class Main {
    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        File dir = new File("/home/artem/Documents/GMO corpus/Test/");
        indexer.index(dir);
    }
}
