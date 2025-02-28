package net.hearnsoft.tcm.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthStateViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isHandling401 = new MutableLiveData<>(false);

    public void startHandling401() {
        isHandling401.postValue(true);
    }

    public void finishHandling401() {
        isHandling401.postValue(false);
    }

    public LiveData<Boolean> getIsHandling401() {
        return isHandling401;
    }

}
