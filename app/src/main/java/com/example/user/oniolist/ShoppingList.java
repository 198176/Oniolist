package com.example.user.oniolist;

public class ShoppingList {

    static final String LISTS = "lists";
    static final String PRODUCTS = "products";
    static final String BOUGHT = "bought";
    static final String LISTOWNER = "listOwner";
    private String id;
    private String listName;
    private String listOwner;
    private boolean isBought;

    public ShoppingList() {}

    public ShoppingList(String listName, String listOwner) {
        this.listName = listName;
        this.listOwner = listOwner;
    }

    public ShoppingList(String listName, boolean isBought) {
        this.listName = listName;
        this.isBought = isBought;
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

    public boolean isBought() {
        return isBought;
    }

    public void setBought(boolean bought) {
        isBought = bought;
    }
}
