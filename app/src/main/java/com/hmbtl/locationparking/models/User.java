package com.hmbtl.locationparking.models;

/**
 * Created by anar on 11/7/17.
 */

public class User {

    private int id;
    private String name, lastName;
    private String picture;

    public User(int id, String name, String lastName,  String picture){
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.picture = picture;
    }

    public int getId() {
        return id;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName(){
        return this.name + " " + this.lastName;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return name;
    }
}
