package com.jim.finansia.utils.reportviews;

import java.util.Map;

public interface CategorySlidingInterface {
    public void onSlide(String id, Map<String, Integer> colorSet, int position, boolean isActive);
}
