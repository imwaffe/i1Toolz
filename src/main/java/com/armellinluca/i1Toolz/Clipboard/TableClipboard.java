package com.armellinluca.i1Toolz.Clipboard;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class TableClipboard<T, M extends Serializable> {

    private final MenuItem copy = new MenuItem("Copy");
    private final MenuItem cut = new MenuItem("Cut");
    private final MenuItem paste = new MenuItem("Paste");

    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent clipboardContent = new ClipboardContent();


    public TableClipboard(TableView<T> table){
        ContextMenu contextMenu = new ContextMenu(copy, cut, paste);
        table.setContextMenu(contextMenu);

        table.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if(mouseEvent.getButton() == MouseButton.SECONDARY){
                if(table.getSelectionModel().getSelectedItems().size() == 0){
                    copy.setDisable(true);
                    cut.setDisable(true);
                    paste.setDisable(false);
                } else {
                    copy.setDisable(false);
                    cut.setDisable(false);
                    paste.setDisable(false);
                }
                if(!clipboard.getContentTypes().contains(getDataFormat())){
                    paste.setDisable(true);
                } else {
                    paste.setDisable(false);
                }
            }
        });

        copy.setOnAction(actionEvent -> {
            toClipboard(table.getSelectionModel().getSelectedItems());
        });
        cut.setOnAction(actionEvent -> {
            toClipboard(table.getSelectionModel().getSelectedItems());
            ArrayList<T> toDelete = new ArrayList<>(table.getSelectionModel().getSelectedItems());
            toDelete.forEach(m -> table.getItems().remove(m));
        });
        paste.setOnAction(actionEvent -> {
            fromClipboard();
        });
    }

    protected abstract DataFormat getDataFormat();
    protected abstract M putElements(ArrayList<T> selectedElements);
    protected abstract void retrieveElements(M clipboardContent);

    private void toClipboard(ObservableList<T> objects){
        ArrayList<T> objectsArrayList = new ArrayList<>(objects);
        clipboardContent.put(getDataFormat(), putElements(objectsArrayList));
        clipboard.setContent(clipboardContent);
    }

    private void fromClipboard(){
        if(!clipboard.getContentTypes().contains(getDataFormat()))
            return;
        retrieveElements((M) clipboard.getContent(getDataFormat()));
    }

}
