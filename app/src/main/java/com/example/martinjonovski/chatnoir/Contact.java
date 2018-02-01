package com.example.martinjonovski.chatnoir;

/**
 * Created by Martin Jonovski on 12/6/2017.
 */
public class Contact {

    //private variables

    String _name;
    String key;

    // Empty constructor
    public Contact() {

    }

    // constructor
    public Contact(String name, String key) {

        this._name = name;
        this.key = key;
    }


    // getting name
    public String getName() {
        return this._name;
    }

    // setting name
    public void setName(String name) {
        this._name = name;
    }

    // getting phone number
    public String getKey() {
        return this.key;
    }

    // setting phone number
    public void setKey(String key) {
        this.key = key;
    }
}