package com.yuntechstudent.yuntechapp.ui.score;

import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class ScoreViewModel extends ViewModel {

    public ObservableArrayList<String> semestersList;

    public ScoreViewModel() { semestersList = new ObservableArrayList<>(); }

    //--------------------處理監聽--------------------//
    public void setSemestersList(Map semesters){
        semestersList.clear();
        for(Object k: semesters.keySet())
            semestersList.add(semesters.get(k).toString());
        Comparator<String> compareBySemesters = (String o1, String o2) -> o1.compareTo(o2);
        Collections.sort(semestersList, Collections.reverseOrder(compareBySemesters));
    }
    public ArrayList getSemestersList(){
        return semestersList;
    }
}

