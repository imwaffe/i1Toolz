package com.armellinluca.i1Toolz.Helpers;

import javafx.scene.text.Text;
import org.jetbrains.annotations.Nullable;

public class TextNumber extends Text {
    public TextNumber(){
        super();
    }

    public void setText(@Nullable Float number, int round){
        if(number == null)
            super.setText(null);
        else if(Float.isNaN(number))
            super.setText("--");
        else
            super.setText(Double.toString(Math.round(number*Math.pow(10,round))/Math.pow(10,round)));
    }
    public void setText(@Nullable Double number, int round){
        if(number == null)
            super.setText(null);
        else if(Double.isNaN(number))
            super.setText("--");
        else
            super.setText(Double.toString(Math.round(number*Math.pow(10,round))/Math.pow(10,round)));
    }
    public void setText(@Nullable Integer number){
        if(number == null)
            super.setText(null);
        else if(Double.isNaN(number))
            super.setText("--");
        else
            super.setText(Integer.toString(number));
    }

    public void setText(@Nullable Float number){
        setText(number, 2);
    }
    public void setText(@Nullable Double number){
        setText(number, 2);
    }
}
