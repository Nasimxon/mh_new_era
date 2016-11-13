package com.jim.pocketaccounter.utils.speech;

import java.util.List;

public interface SpeechListener {
    public void onSpeechEnd(List<String> speechResult);
    public void onSpeechPartialListening(List<String> speechResult);
    public void onChangeState(boolean started);
}
