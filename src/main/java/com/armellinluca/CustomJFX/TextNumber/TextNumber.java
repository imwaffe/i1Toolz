package com.armellinluca.CustomJFX.TextNumber;

import javafx.scene.text.Text;

public class TextNumber extends Text {
    public TextNumber(){
        super();
    }

    public void setText(Float number, int round){
        if(number == null)
            super.setText(null);
        else if(Float.isNaN(number))
            super.setText("--");
        else
            super.setText(Double.toString(Math.round(number*Math.pow(10,round))/Math.pow(10,round)));
    }
    public void setText(Double number, int round){
        if(number == null)
            super.setText(null);
        else if(Double.isNaN(number))
            super.setText("--");
        else
            super.setText(Double.toString(Math.round(number*Math.pow(10,round))/Math.pow(10,round)));
    }
    public void setText(Integer number){
        if(number == null)
            super.setText(null);
        else if(Double.isNaN(number))
            super.setText("--");
        else
            super.setText(Integer.toString(number));
    }

    public void setText(Float number){
        setText(number, 2);
    }
    public void setText(Double number){
        setText(number, 2);
    }
}
