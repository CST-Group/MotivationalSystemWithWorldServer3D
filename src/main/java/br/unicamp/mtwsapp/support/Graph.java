package br.unicamp.mtwsapp.support;

import java.util.List;

/**
 * Created by du on 25/07/17.
 */
public class Graph {
    public Graph(String title, String xTitle, String yTitle, List<Result> results){
        this.title = title;
        this.results = results;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
    }

    public String title;
    public String xTitle;
    public String yTitle;
    public List<Result> results;
}
