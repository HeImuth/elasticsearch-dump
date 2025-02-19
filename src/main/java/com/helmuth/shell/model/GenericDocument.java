package com.helmuth.shell.model;

import java.util.HashMap;
import java.util.Map;

public class GenericDocument extends Document {

    public GenericDocument(Map<String, Object> map) {
        super(null, (HashMap<String, Object>) map);
    }

}
