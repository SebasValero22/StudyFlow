package com.example.studyflow;

import android.app.Application;

import com.example.studyflow.utils.UserSession;

/** Restores the persisted JWT session before any activity runs. */
public class StudyFlowApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserSession.getInstance().init(this);
    }
}
