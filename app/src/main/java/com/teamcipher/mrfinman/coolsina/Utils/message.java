package com.teamcipher.mrfinman.coolsina.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;


import es.dmoral.toasty.Toasty;

public class message {
    public static void error(String msg,Context ctx)
    {
        Toasty.error(ctx,msg, Toast.LENGTH_SHORT,true).show();
    }
    public static void info(String msg,Context ctx)
    {
        Toasty.info(ctx,msg, Toast.LENGTH_SHORT,true).show();;
    }
    public static void success(String msg,Context ctx)
    {
        Toasty.success(ctx,msg, Toast.LENGTH_SHORT,true).show();
    }
    public static void warning(String msg,Context ctx)
    {
        Toasty.warning(ctx,msg, Toast.LENGTH_SHORT,true).show();;
    }
    public static void error(String msg,Context ctx,int length)
    {
        Toasty.error(ctx,msg, length,true).show();
    }
    public static void info(String msg,Context ctx,int length)
    {
        Toasty.info(ctx,msg, length,true).show();;
    }
    public static void success(String msg,Context ctx,int length)
    {
        Toasty.success(ctx,msg, length,true).show();
    }
    public static void warning(String msg,Context ctx,int length)
    {
        Toasty.warning(ctx,msg, length,true).show();;
    }


}
