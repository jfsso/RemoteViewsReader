package jp.yokomark.remoteview.reader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jp.yokomark.remoteview.reader.action.RemoteViewsAction;
import jp.yokomark.remoteview.reader.utils.ClassUtils;

/**
 * @author KeishinYokomaku
 */
public class RemoteViewsReader {
    public static final String TAG = RemoteViewsReader.class.getSimpleName();

    @SuppressWarnings("unchecked")
    public static @Nullable RemoteViewsInfo read(@NonNull Context context, @NonNull RemoteViews remoteViews) {
        if (remoteViews == null) {
            return null;
        }
        Class clazz = ClassUtils.getRemoteViewsClass(remoteViews.getClass());
        try {
            Field actionsField = clazz.getDeclaredField("mActions");
            actionsField.setAccessible(true);
            List<Parcelable> list = (List<Parcelable>) actionsField.get(remoteViews);
            ApplicationInfo applicationInfo = ClassUtils.getApplicationInfo(context, remoteViews, clazz);
            int layoutId = remoteViews.getLayoutId();
            List<RemoteViewsAction> actions = new ArrayList<>(list.size());
            for (Parcelable p : list) {
                Parcel action = Parcel.obtain();
                p.writeToParcel(action, 0);
                action.setDataPosition(0);

                ActionMap mapped = ActionMap.find(action.readInt());
                actions.add(mapped.getUnmarshaller().unmarshal(p, action));
            }
            return new RemoteViewsInfo(applicationInfo, layoutId, actions);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }
}
