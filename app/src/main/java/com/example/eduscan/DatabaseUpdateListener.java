package com.example.eduscan;

public interface DatabaseUpdateListener {

    void onUpdateSuccess();
    void onUpdateFailure(String errorMessage);
}
