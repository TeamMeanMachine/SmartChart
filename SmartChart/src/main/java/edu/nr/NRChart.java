package edu.nr;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by Nashoba1768 on 1/22/2017.
 */
class NRChart extends GenericNRChart
{
    private Series series = new Series();



    public NRChart(SmartChart chart)
    {
        super(chart);
        getData().add(this.series);
    }

    public void addValue(Double x)
    {
        if(this.series.getData().size() == 0)
        {
            startTimeMillis = System.currentTimeMillis();
        }

        double currentTime = (System.currentTimeMillis() - startTimeMillis)/1000d;


        this.series.getData().add(new Data(currentTime, x));
        if (this.series.getData().size() > numberOfSamples) {
            this.series.getData().remove(0);
        }
    }

    @Override
    public void reset()
    {
        this.series.getData().clear();
    }

    @Override
    public void save()
    {
        StringBuilder sb = new StringBuilder();
        for(Object x:this.series.getData()) {
            Data<Double,Double> y = (Data<Double, Double>) x;
            sb.append(y.getXValue());
            sb.append(",");
            sb.append(y.getYValue());
            sb.append('\n');
        }

        String fileName = System.getProperty("user.home") + "\\" + this.chart.getName().replace(' ', '_') + ".csv";

        try {
            // Create the empty file with default permissions, etc.
            Files.createFile(Paths.get(fileName));
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" +
                    " already exists%n", fileName);
        } catch (IOException x) {
            // Some other sort of failure, such as permissions.
            System.err.format("createFile error: %s%n", x);
        }


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getLowestX() {
        if(this.series.getData().size() == 0) {
            return 0;
        }

        return ((Data<Double,Double>) this.series.getData().get(0)).getXValue();
    }

    @Override
    public double getLowestY() {
        if(this.series.getData().size() == 0) {
            return 0;
        }
        double minValue = ((Data<Double,Double>) this.series.getData().get(0)).getYValue();
        for(int i=0; i<this.series.getData().size(); i++){
            if(((Data<Double,Double>) this.series.getData().get(i)).getYValue() < minValue) {
                minValue = ((Data<Double, Double>) this.series.getData().get(i)).getYValue();
            }
        }
        return minValue;
    }

    @Override
    public double getHighestX() {
        if(this.series.getData().size() == 0) {
            return 110;
        }
        return ((Data<Double,Double>) this.series.getData().get(this.series.getData().size()-1)).getXValue();
    }

    @Override
    public double getHighestY() {
        if(this.series.getData().size() == 0) {
            return 110;
        }
        double maxValue = ((Data<Double,Double>) this.series.getData().get(0)).getYValue();
        for(int i=0; i<this.series.getData().size(); i++){
            if(((Data<Double,Double>) this.series.getData().get(i)).getYValue() > maxValue){
                maxValue = ((Data<Double,Double>) this.series.getData().get(i)).getYValue();
            }
        }
        return maxValue;
    }



}
