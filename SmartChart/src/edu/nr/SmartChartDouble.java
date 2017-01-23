package edu.nr;

import dashfx.lib.controls.Category;
import dashfx.lib.controls.Control;
import dashfx.lib.controls.Designable;
import dashfx.lib.data.DataCoreProvider;
import dashfx.lib.data.SmartValue;
import dashfx.lib.data.SmartValueTypes;
import dashfx.lib.data.SupportedTypes;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.io.*;
import java.lang.StringBuilder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Designable(value="SmartChartDouble", image = "/smartchart.png", description="Uses built-in graph and manual list storing. Includes a reset button (wow!)")
@SupportedTypes({SmartValueTypes.String})
@Category("General")
public class SmartChartDouble
        extends GridPane
        implements Control, ChangeListener<Object>
{
    StringProperty name = new SimpleStringProperty();

    @Designable(value="Name", description="The name the control binds to")
    public StringProperty nameProperty()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name.getValue();
    }

    public void setName(String value)
    {
        this.name.setValue(value);
    }

    ChartImpl chartImpl;

    public SmartChartDouble()
    {
        setAlignment(Pos.CENTER);

        chartImpl = new ChartImpl(this);
        add(chartImpl, 0, 0);

        Button resetButton = new Button("Reset Graph");
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                chartImpl.reset();
            }
        });
        add(resetButton, 0, 1, 3, 1);

        Button saveButton = new Button("Save Data");
        saveButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                chartImpl.save();
            }
        });
        add(saveButton, 0, 2, 3, 1);
    }

    public void registered(final DataCoreProvider provider)
    {
        if (getName() != null) {
            provider.getObservable(getName()).addListener(this);
        }
        this.name.addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                if (t != null) {
                    provider.getObservable(t).removeListener(SmartChartDouble.this);
                }
                provider.getObservable(t1).addListener(SmartChartDouble.this);
            }
        });
    }

    public void changed(ObservableValue<? extends Object> ov, Object old, Object t1)
    {
        SmartValue sv = (SmartValue)ov;
        String x = sv.getData().asString();

        chartImpl.addValue(x);
    }

    public Node getUi()
    {
        return this;
    }

    class ChartImpl extends LineChart<Number, Number>
    {
        private ArrayList<Series<Number, Number>> series = new ArrayList<>();

        private boolean firstTime = true;

        private long startTimeMillis;

        double mouseStartY;

        SmartChartDouble chart;

        public ChartImpl(SmartChartDouble chart)
        {
            super(new NumberAxis(), new NumberAxis());

            setAnimated(false);
            ((NumberAxis) getXAxis()).setForceZeroInRange(false);
            ((NumberAxis) getYAxis()).setForceZeroInRange(false);
            setLegendVisible(false);

            this.chart = chart;
        }

        int countNumDelimiter(String str, char delimiter) {
            return str.length() - str.replace(String.valueOf(delimiter), "").length();
        }

        public void addValue(String str)
        {
            ArrayList<Double> inputs = new ArrayList<>();

            int numDelimiters = countNumDelimiter(str, ':');

            if(firstTime) {
                for(int i = 0; i < numDelimiters+1; i++)
                    series.add(new Series<>());

                for(Series<Number, Number> s : series) {
                    getData().add(s);
                }

                firstTime = false;
            }

            System.err.println("String input: " + str);
            System.err.println("Num delimiters: " + numDelimiters);
            int prevIndex = -1;
            for (int i = 0; i < numDelimiters; i++) {
                if(prevIndex + 1 == str.length()) {
                    System.err.println("Something's screwy with num delimiters, it says there are fewer than were found originally");
                }
                int currentIndex = str.indexOf(':', prevIndex+1);
                System.err.println("Current index: " + currentIndex);
                System.err.flush();
                inputs.add(Double.parseDouble(str.substring(prevIndex + 1, currentIndex)));
                prevIndex = currentIndex;
            }
            inputs.add(Double.parseDouble(str.substring(prevIndex + 1)));

            inputs.forEach( y -> {
                System.err.println("Val: " + y);
            });
            System.err.flush();

            final boolean[] startFlag = {true};

            series.forEach(z -> {
                if(z.getData().size() != 0) startFlag[0] = false;
            });

            if(startFlag[0])
            {
                startTimeMillis = System.currentTimeMillis();
            }

            double currentTime = (System.currentTimeMillis() - startTimeMillis)/1000d;

            for(int i = 0; i < inputs.size(); i++) {
                series.get(i).getData().add(new Data(currentTime, inputs.get(i)));
            }

            series.forEach(s -> {
                if (s.getData().size() > 500) s.getData().remove(0);
            });

        }

        public void reset()
        {
            series.forEach(s -> {
                s.getData().clear();
            });
        }

        public void save()
        {
            StringBuilder sb = new StringBuilder();
            series.forEach(s -> {
                for(Data<Number, Number> x:s.getData()) {
                    sb.append(x.getXValue());
                    sb.append(",");
                    sb.append(x.getYValue());
                    sb.append(",,");
                }
                sb.append('\n');
            });


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


    }

}

