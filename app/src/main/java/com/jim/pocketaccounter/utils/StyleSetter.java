package com.jim.pocketaccounter.utils;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.view.View;
import android.widget.ImageView;

import com.jim.pocketaccounter.utils.catselector.DrawingSelectorView;

import java.lang.reflect.Field;

public class StyleSetter {
    private Object object;
    private int color;
    private SharedPreferences preferences;
    public StyleSetter(Object object, SharedPreferences preferences) {
        this.object = object;
        this.preferences = preferences;
    }

    public void set() {
        boolean isDrawingSelectorView = false;
        if (object.getClass().getSuperclass().getName().equals(DrawingSelectorView.class.getName())) {
            Field[] fields = object.getClass().getSuperclass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Styleable.class)) {
                    try {
                        field.setAccessible(true);
                        color = Color.parseColor(preferences.getString(field.getAnnotation(Styleable.class).colorLayer(), "#d4e1e1"));
                        ImageView imageView = (ImageView) field.get(object);
                        imageView.setColorFilter(color);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        isDrawingSelectorView = true;
                    }
                }
            }

        }
        if (isDrawingSelectorView) return;
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Styleable.class)) {
                color = Color.parseColor(preferences.getString(field.getAnnotation(Styleable.class).colorLayer(), "#d4e1e1"));
                try {
                    field.setAccessible(true);
                    //ImageView fields
                    if (field.getType().getName().equals(ImageView.class.getName())) {
                        ImageView imageView = (ImageView) field.get(object);
                        imageView.setColorFilter(color);
                        return;
                    }

                    //ViewGroup for giving color of background
                    Class deeper = field.getType().getSuperclass();
                    while (deeper.getSuperclass() != null ||
                            !deeper.getSuperclass().getName().equals(Object.class.getName())) {
                        deeper = deeper.getSuperclass();
                        if (deeper.getName().equals(View.class.getName())) {
                            View view = (View) field.get(object);
                            view.setBackgroundColor(color);
                            break;
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
