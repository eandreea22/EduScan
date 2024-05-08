package com.example.eduscan;

import android.content.Intent;

import java.util.ArrayList;

public class User {

    private String name;
    private String email;
    private String password;
    private String id;

    private ArrayList<FileModel> files;

    public User() {
    }

    public User(String name,String email, String password) {
        this.name = name;
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

    public void removeFiles(ArrayList<String> deleteFiles){

        ArrayList<Integer> positionfiles = new ArrayList<>();

        for (String file: deleteFiles) {

            for (int i = 0 ; i < files.size(); i++){
                if (files.get(i).getFileName().equals(file)){
                    positionfiles.add(i);
                    break;
                }
            }
        }

        for (int i=0; i< positionfiles.size(); i++){
            files.remove((int) positionfiles.get(i));
        }
        System.out.println(files);

    }


}
