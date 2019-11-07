package com.ysteimle.segproject.easywalkin;

public class Service {
    public String id;
    public String name;
    public String provider;
    //public String category;
    public String description;

    public Service () {}

    public Service (String id, String name, String provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    public Service (String id, String name, String provider, String description) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.description = description;
    }
}
