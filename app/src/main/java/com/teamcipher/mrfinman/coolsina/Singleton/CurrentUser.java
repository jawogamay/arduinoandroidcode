package com.teamcipher.mrfinman.coolsina.Singleton;

import com.teamcipher.mrfinman.coolsina.Model.User;

public class CurrentUser {
    private static CurrentUser instance;
    private static User currentUser;

    public static CurrentUser getInstance()
    {
        if (instance == null)
            instance = new CurrentUser();
        return instance;
    }

    private CurrentUser() {
        currentUser = new User();
    }

    public User getCurrentUser()
    {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        CurrentUser.currentUser = currentUser;
    }
}
