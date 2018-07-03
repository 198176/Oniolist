package com.example.user.oniolist;

public class ShoppingList {

    private String id;
    private String listName;
    private String listOwner;

    public ShoppingList() {}

    public ShoppingList(String listName, String listOwner) {
        this.listName = listName;
        this.listOwner = listOwner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListOwner() {
        return listOwner;
    }

    public void setListOwner(String listOwner) {
        this.listOwner = listOwner;
    }
}
