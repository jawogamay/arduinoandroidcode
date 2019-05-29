package com.teamcipher.mrfinman.coolsina.Singleton;

import com.teamcipher.mrfinman.coolsina.Model.User;

import java.util.ArrayList;

public class UserList {
    private static UserList instance;
    private static ArrayList<User> userlist;

    private UserList()
    {
        userlist = new ArrayList<>();
    }

    public static UserList getInstance() {
        if (instance == null)
            instance = new UserList();
        return instance;
    }

    public ArrayList<User> getUserlist() {

        return userlist;
    }

    public void addNewUser(User u) {
        for (User user : userlist)
        {
            if (user != u)
            {
                getUserlist().add(u);
            }
        }
    }
}
