package com.ysteimle.segproject.easywalkin;

public class Service {
    public String id;
    public String name;
    public String provider;
    public String category;
    public String description;

    public Service () {}

    public Service (String name, String provider) {
        this.name = name;
        this.provider = provider;
        // Should I set up a default category?
    }

    public Service (String id, String name, String provider, String category, String description) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.category = category;
        this.description = description;
    }
}
