package com.jim.pocketaccounter.utils.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;

import com.jim.pocketaccounter.PocketAccounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class PASpeechRecognizer implements RecognitionListener {

    // ----- TYPES ----- //
    // Timer task used to reproduce the timeout input error that seems not be called on android 4.1.2
    public class SilenceTimer extends TimerTask {
        @Override
        public void run() {
            Log.d("sss", "timeout of timer");
            onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
        }
    }
    // ---- MEMBERS ---- //
    // Speech recognizer instance
    private SpeechRecognizer speech = null;
    // Timer used as timeout for the speech recognition
//    private Timer speechTimeout = null;
    // Context
    private Context context;
    // ---- METHODS ---- //
    // Lazy instantiation method for getting the speech recognizer

    // Audio Manager for mute and unmute beep sound
    private AudioManager amanager;
    int mStreamVolume = 0;
    //Speech listener for passing data to listening Fragment
    private SpeechListener listener;
    // Timer used as timeout for the speech recognition
//    private Timer speechTimeout = null;

    private SpeechRecognizer getSpeechRevognizer(){
        if (speech == null) {
            speech = SpeechRecognizer.createSpeechRecognizer(context);
            speech.setRecognitionListener(this);
        }
        return speech;
    }

    public PASpeechRecognizer(Context context) {
        this.context = context;
        amanager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }
    /**
     * onCreateView(LayoutInflater, ViewGroup, Bundle) creates and returns the view hierarchy associated with the fragment.
     */
    public void startVoiceRecognitionCycle()
    {
        mStreamVolume = amanager.getStreamVolume(AudioManager.STREAM_MUSIC); // getting system volume into var for later un-muting
        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        //Intent for recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 250);
        getSpeechRevognizer().startListening(intent);
    }

    /**
     * Stop the voice recognition process and destroy the recognizer.
     */
    public void stopVoiceRecognition()
    {
//        speechTimeout.cancel();
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
    }

    /* RecognitionListener interface implementation */
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d("sss","onReadyForSpeech");
//        speechTimeout = new Timer();
//        speechTimeout.schedule(new SilenceTimer(), 3000);
        // create and schedule the input speech timeout
        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("sss","onBeginningOfSpeech");
        // Cancel the timeout because voice is arriving
//        if (speechTimeout != null)
//            speechTimeout.cancel();
        if (listener != null)
            listener.onChangeState(true);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("sss","onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("sss","onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        String message;
        switch (error)
        {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:

                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Not recognised";
                break;
        }
        Log.d("sss","onError code:" + error + " message: " + message);
        ((PocketAccounter)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSpeechRevognizer().cancel();
                if (listener != null)
                    listener.onChangeState(false);
            }
        });
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d("sss","onEvent");
    }

    @Override
    public void onPartialResults(Bundle results) {
        if ((results != null)
                && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))
        {
            List<String> heard =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (String text : heard) {
                Log.d("sss", text);
            }
            listener.onSpeechPartialListening(heard);
        }
    }

    @Override
    public void onResults(Bundle results) {
        startVoiceRecognitionCycle();
        // Restart new dictation cycle
        StringBuilder scores = new StringBuilder();
        for (int i = 0; i < results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES).length; i++) {
            scores.append(results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)[i] + " ");
        }
        Log.d("sss","onResults: " + results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) + " scores: " + scores.toString());
        if (listener != null)
            listener.onSpeechEnd(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));

    }

    public void setSpeechListener(SpeechListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
		Log.d("sss","onRmsChanged "+rmsdB);
    }
}
