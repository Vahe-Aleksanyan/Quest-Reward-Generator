import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.SimpleCurveFitter;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;


public class QuestGenerator {

    public static void main(String[] args) {

        // I converted the given data example into these coordinates, positive values represent the number of b, the negative ones represent s
        int[][] xyData = {
                {1, 9}, {2, -1}, {3, 8}, {4, -1}, {5, 3}, {6, -1}, {7, 3}, {8, -1}, {9, 4}, {10, -1},
                {11, 1}, {12, -1}, {13, 2}, {14, -1}, {15, 1}, {16, -1}, {17, 2}, {18, -1}, {19, 2}, {20, -1},
                {21, 1}, {22, -1}, {23, 1}, {24, -1}, {25, 1}, {26, -1}, {27, 1}, {28, -2}, {29, 1}, {30, -1},
                {31, 1}, {32, -1}, {33, 1}, {34, -1}, {35, 1}, {36, -1}, {37, 1}, {38, -3}, {39, 1}, {40, -3},
                {41, 1}, {42, -2}, {43, 1}, {44, -2}, {45, 1}, {46, -5}, {47, 1}, {48, -6}, {49, 1}, {50, -10}
        };

        ArrayList<Integer> xPos = new ArrayList<>();
        ArrayList<Integer> yPos = new ArrayList<>();
        ArrayList<Integer> xNeg = new ArrayList<>();
        ArrayList<Integer> yNeg = new ArrayList<>();

        // then I separated the positives and negatives
        for (int[] xy : xyData) {
            int x = xy[0];
            int y = xy[1];
            if (y > 0) {
                xPos.add(x);
                yPos.add(y);
            } else if (y < 0) {
                xNeg.add(x);
                yNeg.add(y);
            }
        }

        double[] xPosArray = xPos.stream().mapToDouble(Integer::intValue).toArray();
        double[] yPosArray = yPos.stream().mapToDouble(Integer::intValue).toArray();
        double[] xNegArray = xNeg.stream().mapToDouble(Integer::intValue).toArray();
        double[] yNegArray = yNeg.stream().mapToDouble(Integer::intValue).toArray();

        // Perform cubic spline interpolation for positive values
        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction splineFunctionPos = splineInterpolator.interpolate(xPosArray, yPosArray);

        // Perform cubic spline interpolation for negative values
        PolynomialSplineFunction splineFunctionNeg = splineInterpolator.interpolate(xNegArray, yNegArray);

        // Fit exponential curves for extrapolation using all points
        double[] posExpCoeff = fitExponential(xPosArray, yPosArray);
        double[] negExpCoeff = fitExponential(xNegArray, yNegArray);

        ArrayList<Double> combinedValues = new ArrayList<>();

        for (int x = -50; x <= 100; x++) {
            double yValue = Double.NaN;
            if (x % 2 == 0) {
                if (x < xNegArray[0] || x > xNegArray[xNegArray.length - 1]) {
                    yValue = evalExponential(x, negExpCoeff); // Extrapolate using exponential
                } else {
                    yValue = splineFunctionNeg.value(x);
                }
            } else {
                if (x < xPosArray[0] || x > xPosArray[xPosArray.length - 1]) {
                    yValue = evalExponential(x, posExpCoeff); // Extrapolate using exponential
                } else {
                    yValue = splineFunctionPos.value(x);
                }
            }
            combinedValues.add(yValue);
        }

        // Convert combined values to integers
        ArrayList<Integer> combinedIntValues = new ArrayList<>();
        for (double value : combinedValues) {
            int intValue = (int) Math.round(value);
            if (intValue == 0 && value != 0) {
                intValue = value < 0 ? -1 : 1;
            }
            combinedIntValues.add(intValue);
        }

        //check the result using constructStr function with different values
        System.out.println("result for N = 100   " + constructStr(combinedIntValues, 100));
        System.out.println("result for N = 500   " + constructStr(combinedIntValues, 500));
        System.out.println("result for N = 2000  " + constructStr(combinedIntValues, 2000));
        System.out.println("result for N = 10000   " + constructStr(combinedIntValues, 10000));

        // Plot the data
//        plotData(combinedIntValues, "Quest Rewards", "X", "Y");
    }

    private static double[] fitExponential(double[] x, double[] y) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < x.length; i++) {
            obs.add(x[i], y[i]);
        }

        // Exponential model: y = a * exp(b * x)
        ParametricUnivariateFunction exponentialFunction = new ParametricUnivariateFunction() {
            @Override
            public double value(double x, double... parameters) {
                double a = parameters[0];
                double b = parameters[1];
                return a * Math.exp(b * x);
            }

            @Override
            public double[] gradient(double x, double... parameters) {
                double a = parameters[0];
                double b = parameters[1];
                return new double[]{Math.exp(b * x), a * x * Math.exp(b * x)};
            }
        };

        SimpleCurveFitter fitter = SimpleCurveFitter.create(exponentialFunction, new double[]{1, 0.1});
        return fitter.fit(obs.toList());
    }

    private static double evalExponential(double x, double[] coeff) {
        // Evaluate the exponential model y = a * exp(b * x)
        return coeff[0] * Math.exp(coeff[1] * x);
    }



    public static String constructStr(ArrayList<Integer> arr, int N) {
        // start at the middle of the array
        int mid = 75;
        // here i use string buffer for efficient insertioan and uddates of string
        StringBuffer sb = new StringBuffer();
        sb.append('b');
        while(mid < arr.size() - 1) {

            // for positives append b
            if(arr.get(mid + 1) > 0) {

                for(int i = 0; i < arr.get(mid + 1); i++) {
                    sb.append("b");
                    if(sb.length()  == N) {
                        return sb.toString();
                    }
                }

                for(int i = 0; i < arr.get(arr.size() - mid - 2); i++) {
                    sb.insert(0, "b");
                    if(sb.length()  == N) {
                        return sb.toString();
                    }
                }
            }
            // for negative values append s
            if(arr.get(mid + 1) < 0) {

                int val = Math.abs(arr.get(mid + 1));
                for(int i = 0; i < val; i++) {
                    sb.append("s");
                    if(sb.length()  == N) {
                        return sb.toString();
                    }
                }
                int val2 = Math.abs(arr.get(arr.size() - mid - 2));
                for(int i = 0; i < val2 ; i++) {
                    sb.insert(0, "s");
                    if(sb.length()  == N) {
                        return sb.toString();
                    }
                }
            }
            mid++;
        }
        return sb.toString();
    }

    public static void plotData(ArrayList<Integer> yData, String chartTitle, String xAxisLabel, String yAxisLabel) {
        // Create a series to add data points
        XYSeries series = new XYSeries("Data Points");
        for (int i = 0; i < yData.size(); i++) {
            series.add(i - 50, yData.get(i)); // assuming x range starts from -50
        }

        // Create a dataset and add the series to it
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // Create a chart with the dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                chartTitle,     // Chart title
                xAxisLabel,     // X-axis label
                yAxisLabel,     // Y-axis label
                dataset,        // Dataset
                PlotOrientation.VERTICAL,
                true,           // Include legend
                true,
                false
        );

        // Display the chart in a window
        JFrame chartWindow = new JFrame(chartTitle);
        chartWindow.setContentPane(new ChartPanel(chart));
        chartWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chartWindow.pack();
        chartWindow.setLocationRelativeTo(null);
        chartWindow.setVisible(true);
    }

}