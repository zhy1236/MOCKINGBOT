package com.example.hand.mockingbot.utils;

/**zhy
 * Created by liuweiyan_PC on 2016/9/13.
 */
public class Utils {
    //这个是在主线程去更新ui,在没有上下文的环境,
    public static void runOnUIThread(Runnable runnable) {
        HandApp.mainHandler.post(runnable);

    }
}
