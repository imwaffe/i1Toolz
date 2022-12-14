package com.armellinluca.i1Toolz.Helpers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ResizeHover<T> {
    private Boolean enabled = false;

    public ResizeHover(Pane pane, WritableValue<T> target, T enterSize, double milliseconds){
        this(pane, target, enterSize, target.getValue(), milliseconds);
    }

    public ResizeHover(Pane pane, WritableValue<T> target, T leaveSize, T enterSize, double milliseconds){
        Duration animationDuration = new Duration(milliseconds);
        Timeline onEnter = new Timeline(new KeyFrame(animationDuration, new KeyValue(target, enterSize, Interpolator.EASE_BOTH)));
        Timeline onLeave = new Timeline(new KeyFrame(animationDuration, new KeyValue(target, leaveSize, Interpolator.EASE_BOTH)));
        pane.hoverProperty().addListener((observableValue, oldValue, newValue) -> {
            if(this.isEnabled()) {
                if (newValue) {
                    onLeave.stop();
                    onEnter.play();
                } else {
                    onEnter.stop();
                    onLeave.play();
                }
            }
        });
    }

    public void enable(){
        this.enabled = true;
    }
    public void disable(){
        this.enabled = false;
    }
    public boolean isEnabled(){
        return enabled;
    }
}
