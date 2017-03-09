package edu.nr;

import dashfx.lib.controls.Category;
import dashfx.lib.controls.Control;
import dashfx.lib.controls.Designable;
import dashfx.lib.data.DataCoreProvider;
import dashfx.lib.data.SmartValue;
import dashfx.lib.data.SmartValueTypes;
import dashfx.lib.data.SupportedTypes;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

@Designable(value="SmartChartDouble", image = "/smartchart.png", description="Uses built-in graph and manual list storing. Includes a reset button (wow!)")
@SupportedTypes({SmartValueTypes.String})
@Category("General")
public class SmartChartDouble extends GenericSmartChart
{

    NRChartDouble chart;

    public SmartChartDouble()
    {
        setAlignment(Pos.CENTER);

        chart = new NRChartDouble(this);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(100);
        getColumnConstraints().addAll(column1); // we want 100% of width

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(100);
        getRowConstraints().addAll(row1); // we want 100% of height

        add(chart, 0, 0);

        Button resetButton = new Button("Reset Graph");
        resetButton.setOnAction(event -> chart.reset());

        Button saveButton = new Button("Save Data");
        saveButton.setOnAction(event -> chart.save());

        final Rectangle zoomRect = new Rectangle();
        zoomRect.setManaged(false);
        zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
        getChildren().add(zoomRect);

        chart.setUpZooming(zoomRect);

        final Button zoomButton = new Button("Zoom");
        final Button resetZoomButton = new Button("Reset Zoom");
        zoomButton.setOnAction(event -> chart.doZoom(zoomRect));
        resetZoomButton.setOnAction(event -> {
            final NumberAxis xAxis = (NumberAxis)chart.getXAxis();
            xAxis.setLowerBound(chart.getLowestX());
            xAxis.setUpperBound(chart.getHighestX());
            final NumberAxis yAxis = (NumberAxis)chart.getYAxis();
            yAxis.setLowerBound(chart.getLowestY());
            yAxis.setUpperBound(chart.getHighestY());

            zoomRect.setWidth(0);
            zoomRect.setHeight(0);
        });

        final Button prepareToZoomButton = new Button("Prepare to zoom");
        prepareToZoomButton.setOnAction(event -> {
            if(chart.isAutoZooming()) {
                chart.setAutoZooming(false);
                prepareToZoomButton.setText("Finish zooming");
            } else {
                chart.setAutoZooming(true);
                prepareToZoomButton.setText("Prepare to zoom");
            }
        });

        final BooleanBinding disableControls =
                zoomRect.widthProperty().lessThan(5)
                        .or(zoomRect.heightProperty().lessThan(5)).or(chart.isAutoZooming);
        zoomButton.disableProperty().bind(disableControls);
        resetZoomButton.disableProperty().bind(chart.isAutoZooming);

        HBox hb = new HBox();
        hb.getChildren().addAll(resetButton, saveButton, zoomButton, resetZoomButton, prepareToZoomButton);
        hb.setSpacing(10);
        add(hb, 0, 1);
    }


    public void changed(ObservableValue<? extends Object> ov, Object old, Object t1) {
        SmartValue sv = (SmartValue) ov;
        String x = sv.getData().asString();

        chart.addValue(x);
    }


}


