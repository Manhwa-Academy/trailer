package com.mari.magic.model;

import java.util.List;

public class DrawerMenuItem {

    public String title;
    public boolean expandable;
    public boolean expanded;
    public List<String> children;

    public DrawerMenuItem(String title, boolean expandable){
        this.title = title;
        this.expandable = expandable;
    }

}