package jrfeng.player.player;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Scanner;

public class Configure {
    private static Class mPendingActivityCL;
    private static Class mNotificationViewCL;

    public static void decode(Context context) throws XmlPullParserException, IOException, ClassNotFoundException {
        //从配置文件解析
        InputStream inputStream = context.getAssets().open("music_player.xml");
        StringBuilder builder = new StringBuilder(128);
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();
        String content = builder.toString();
        //解析XML
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(content));
        String pendingActivity = "";
        String notificationView = "";
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String nodeName = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (nodeName.equalsIgnoreCase("pending-activity")) {
                    pendingActivity = parser.nextText();
                } else if (nodeName.equalsIgnoreCase("notify-view")) {
                    notificationView = parser.nextText();
                }
            }
            eventType = parser.next();
        }
        mPendingActivityCL = Class.forName(pendingActivity);

        if (!notificationView.equals("")) {
            mNotificationViewCL = Class.forName(notificationView);
        } else {
            mNotificationViewCL = DefaultNotifyControllerView.class;
        }
    }

    public static Class getPendingActivityClass() {
        return mPendingActivityCL;
    }

    public static Class getNotificationViewClass() {
        return mNotificationViewCL;
    }
}
