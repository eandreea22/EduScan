package com.example.eduscan;

import java.util.ArrayList;

public class User {

    private String name;
    private String username;
    private String email;
    private String password;
    private String id;

    private ArrayList<FileModel> files;

    public User() {
    }

    public User(String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.files = new ArrayList<FileModel>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addFile(FileModel file){
        files.add(file);
    }

    public ArrayList<FileModel> getFiles() {
        return files;
    }

    public void editFileName(String oldName, String newName){

        for (int i=0; i<files.size(); i++){
            if (files.get(i).getFileName().equals(oldName)){
                files.get(i).setFileName(newName);
                break;
            }
        }


    }


}
