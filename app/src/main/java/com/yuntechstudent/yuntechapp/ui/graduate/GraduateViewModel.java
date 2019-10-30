package com.yuntechstudent.yuntechapp.ui.graduate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GraduateViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GraduateViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}