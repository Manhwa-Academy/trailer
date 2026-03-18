package com.mari.magic.utils;

import com.mari.magic.model.Anime;
import java.util.List;

public interface ScheduleListener {
    void onScheduleLoaded(List<Anime> scheduleList, int currentPage, int lastPage);
    void onScheduleError(String error);
}