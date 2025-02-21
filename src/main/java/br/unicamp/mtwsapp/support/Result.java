package br.unicamp.mtwsapp.support;

/**
 * Created by du on 25/07/17.
 */
public class Result {

    public Result(String variableName, Object x, Object y) {
        this.variableName = variableName;
        this.x = x;
        this.y = y;
    }

    public String variableName;
    public Object x;
    public Object y;
}
