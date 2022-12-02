package com.armellinluca.i1Toolz.Helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class BackwardCompatibleInputStream extends ObjectInputStream {

    public BackwardCompatibleInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

        try {
            Class.forName(resultClassDescriptor.getName());
        } catch (ClassNotFoundException ignore){
            String legacyClassName = resultClassDescriptor.getName().substring(resultClassDescriptor.getName().lastIndexOf(".") + 1);
            resultClassDescriptor = ObjectStreamClass.lookup(Class.forName("com.armellinluca.i1Toolz."+resultClassDescriptor.getName()));
        }

        return resultClassDescriptor;
    }
}
