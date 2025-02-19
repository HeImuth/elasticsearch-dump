package com.helmuth.shell.model;

import java.util.HashMap;

public class Document extends HashMap<String, Object> {
    public String _id;

    public Document() {
    }

    public Document(String _id, HashMap<String, Object> map) {
        super(map);
        this._id = _id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public String toString() {
        return "{_id='" + _id + '\'' + '}' + " " + super.toString();
    }
}
