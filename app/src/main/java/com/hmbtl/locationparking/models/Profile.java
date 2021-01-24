package com.hmbtl.locationparking.models;

/**
 * Created by anar on 11/7/17.
 */

public class Profile extends User {

    private String email;

    public Profile(int id, String email, String firstName, String lastName, String picture){
        super(id, firstName, lastName, picture);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
