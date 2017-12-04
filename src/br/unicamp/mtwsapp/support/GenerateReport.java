package br.unicamp.mtwsapp.support;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by du on 06/04/17.
 */
public class GenerateReport {

    private static final String REPORT_FOLDER = "reportFiles/";
    private static List<ChartFrame> chartFrames;
    private static final Shape circle = new Ellipse2D.Double(-3, -3, 6, 6);
    private static final Color line = Color.gray;

    public static void main(String args[]){

        chartFrames = new ArrayList<>();

        File directory = new File(REPORT_FOLDER);
        File[] contents = directory.listFiles();

        List<File> files = Arrays.stream(contents).filter(file -> !file.getName().equals(".DS_Store")).collect(Collectors.toList());

        files.stream().forEach( file -> {

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String sResult = bufferedReader.readLine();

                Gson gson = new Gson();
                Type listType = new TypeToken<Graph>(){}.getType();
                Graph graph = gson.fromJson(sResult,  listType);

                createChart(graph, graph.title);

                System.out.println("\n\nGrafico:"+graph.title);

                //showStatics(graph);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static double getVariance(List<Result> results, double mean){

        double sum = 0;


        for (Result result: results) {
            sum += Math.pow((double)result.y-mean,2);
        }

        return sum/results.size();
    }


    private static void getStatics(List<Result> results, String title){

        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

        for (int i = 0; i < results.size(); i++) {
            descriptiveStatistics.addValue((double)results.get(i).y);
        }

        DecimalFormat df=new DecimalFormat("0.00");

        System.out.println("#############EXPERIMENTO:"+title+"##############");
        System.out.println("\\multicolumn{1}{|p{1cm}|}{\\centering "+ df.format(descriptiveStatistics.getMean()).replace(",", ".")+ "} &" +
                " \\multicolumn{1}{|p{1.5cm}|}{\\centering "+ df.format(descriptiveStatistics.getPercentile(50)).replace(",", ".")+ "} &" +
                " \\multicolumn{1}{|p{2cm}|}{\\centering "+ df.format(descriptiveStatistics.getStandardDeviation()).replace(",", ".")+ "} &" +
                " \\multicolumn{1}{|p{2cm}|}{\\centering "+df.format(descriptiveStatistics.getPopulationVariance()).replace(",", ".")+"}");



        Result minResult = results.stream().filter(result -> (double) result.y == descriptiveStatistics.getMin()).findFirst().get();
        Result maxResult = results.stream().filter(result -> (double) result.y == descriptiveStatistics.getMax()).findFirst().get();

        System.out.println("Min:"+descriptiveStatistics.getMin()+"/"+minResult.x + "s");
        System.out.println("Max:"+descriptiveStatistics.getMax()+"/"+maxResult.x + "s");

        /*System.out.println("#############EXPERIMENTO:"+title+"##############");

        System.out.println("Variancia: " + df.format(descriptiveStatistics.getVariance()));

        System.out.println("Mediana: " + df.format(descriptiveStatistics.getPercentile(50)));

        System.out.println("Media: " + df.format(descriptiveStatistics.getMean()));

        System.out.println("Desvio Padrao: " + df.format(descriptiveStatistics.getStandardDeviation()));*/

    }


    private static void showStatics(Graph graph){

        List<Result>results = new ArrayList<>();

        for (int i = 1; i <= 10 ; i++) {

            if (i == 1) {
                results = graph.results.stream().filter(result -> result.variableName.equals("1st")).collect(Collectors.toList());
            } else if(i ==2){
                results = graph.results.stream().filter(result -> result.variableName.equals("2nd")).collect(Collectors.toList());
            } else if(i == 3)
            {
                results = graph.results.stream().filter(result -> result.variableName.equals("3rd")).collect(Collectors.toList());
            } else{
                int finalI = i;
                results = graph.results.stream().filter(result -> result.variableName.equals(String.valueOf(finalI)+"th")).collect(Collectors.toList());
            }

            getStatics(results, String.valueOf(i));


        }

    }


    private static ChartFrame createChart(Graph graph, String title){

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        graph.results.stream().forEach(result -> {
            List<XYSeries> xySeries =  xySeriesCollection.getSeries();
            List<XYSeries> foundSerie = xySeries.stream().filter(xy -> xy.getKey().equals(result.variableName)).collect(Collectors.toList());
            if(foundSerie.size() > 0){
                foundSerie.get(0).add((double)result.x, (double)result.y);
            }
            else{
                XYSeries serie = new XYSeries(result.variableName);
                serie.add((double)result.x, (double)result.y);
                xySeriesCollection.addSeries(serie);
            }




        });


        /*XYSeries experimentAverage = new XYSeries("Experiments' Average");

        List<Result> average = new ArrayList<>();

        for (double i=1; i <= 600; i++){

            double finalI = i;

            List<Result> timeResult = graph.results.stream().filter(result -> ((double)result.x) == finalI).collect(Collectors.toList());

            final double[] meanY = {0};

            timeResult.stream().forEach(result -> {
                meanY[0] += (double)result.y;

            });

            average.add(new Result(String.valueOf(i), i, meanY[0]/timeResult.size()));

            experimentAverage.add(i, meanY[0]/timeResult.size());
        }

        getStatics(average, "Media Total");


        xySeriesCollection.addSeries(experimentAverage);*/

        XYLineAndShapeRenderer renderer0 = new XYLineAndShapeRenderer();


        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                graph.xTitle,
                graph.yTitle,
                xySeriesCollection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        //TimeSeries dataset3 = MovingAverage.createMovingAverage(t1, "LT", 49, 49);

        //XYDataset experimentAverage = MovingAverage.createMovingAverage(xySeriesCollection, "Experiment Average", 50, 0);


        XYPlot xyPlot = (XYPlot) chart.getPlot();
        //xyPlot.setDataset(0, experimentAverage);
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);


        MyRenderer renderer = new MyRenderer(true, false, 600);
        xyPlot.setRenderer(renderer);
        renderer.setSeriesShape(10, circle);
        renderer.setSeriesPaint(10, line);
        renderer.setUseFillPaint(true);
        renderer.setSeriesShapesFilled(10, true);
        renderer.setSeriesShapesVisible(10, true);
        renderer.setUseOutlinePaint(true);
        renderer.setSeriesOutlinePaint(10, line);
        ValueAxis range = xyPlot.getRangeAxis();
        range.setLowerBound(0.5);

        Font font3 = new Font("Dialog", Font.PLAIN, 20);
        xyPlot.getDomainAxis().setLabelFont(font3);
        xyPlot.getRangeAxis().setLabelFont(font3);

        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(0.00, 600);

        xyPlot.setBackgroundPaint(Color.lightGray);
        xyPlot.setDomainGridlinePaint(Color.white);
        xyPlot.setRangeGridlinePaint(Color.white);
        chart.setBackgroundPaint(Color.white);

        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);

        return frame;
    }

    private static class MyRenderer extends XYLineAndShapeRenderer {

        private List<Color> clut;

        public MyRenderer(boolean lines, boolean shapes, int n) {
            super(lines, shapes);
            clut = new ArrayList<Color>(n);
            for (int i = 0; i < n; i++) {
                clut.add(Color.getHSBColor((float) i / n, 1, 1));
            }
        }

        @Override
        public Paint getItemFillPaint(int row, int column) {
            return clut.get(column);
        }
    }

    public static class Moda {
        private Map valores = new HashMap();
        public Moda() {}
        public void adicionar(double numero) {
            Double n = new Double(numero);
            /** Verifica se já existe esse elemento no mapa */
            if (valores.get(n) != null) {
                valores.put(n, new Double(((Integer)valores.get(n)).intValue() + 1));
            }
            else {
                valores.put(n, new Double(1));
            }
        }
        public Map getValores() {
            return valores;
        }
        public Set calcular() {
            /** Maior valor encontrado até o momento */
            Double maior = null;
            Set resultado = new HashSet();
            Iterator iterator = valores.keySet().iterator();
            while (iterator.hasNext()) {
                /** Número atual sendo avaliado */
                Double valor = (Double)iterator.next();
                /** Quantidade de ocorrências do número atual */
                Double current = (Double)valores.get(valor);
                if (maior == null) {
                    maior = current;
                }
                /** Encontrou um número com mais ocorrências */
                if (maior.compareTo(current) <= 0) {
                    maior = current;
                    resultado.add(valor);
                }
            }
            iterator = resultado.iterator();
            while (iterator.hasNext()) {
                Integer numero = (Integer)iterator.next();
                /**
                 * Não tem o mesmo número de ocorrências que o maior número de
                 * ocorrências encontrado?
                 */
                if (((Double)valores.get(numero)).compareTo(maior) < 0) {
                    iterator.remove(); // Já era!
                }
            }
            return resultado;
        }

    }

}








