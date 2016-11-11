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
import java.util.Timer;
import java.util.TimerTask;

import me.itangqi.waveloadingview.WaveLoadingView;


public class PASpeechRecognizer implements RecognitionListener {

    // ----- TYPES ----- //
    // Timer task used to reproduce the timeout input error that seems not be called on android 4.1.2
    public class SilenceTimer extends TimerTask {
        @Override
        public void run() {
            onError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT);
        }
    }
    // ---- MEMBERS ---- //
    // Speech recognizer instance
    private SpeechRecognizer speech = null;
    // Timer used as timeout for the speech recognition
    private Timer speechTimeout = null;
    // Context
    private Context context;
    // ---- METHODS ---- //
    // Lazy instantiation method for getting the speech recognizer

    // Audio Manager for mute and unmute beep sound
    private AudioManager amanager;
    int mStreamVolume = 0;
    //Speech listener for passing data to listening Fragment
    private SpeechListener listener;
    //state of listening
    private ListeningOfSpeechListener listeningOfSpeechListener;
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

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100000);

        getSpeechRevognizer().startListening(intent);
        mStreamVolume = amanager.getStreamVolume(AudioManager.STREAM_MUSIC); // getting system volume into var for later un-muting
        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    /**
     * Stop the voice recognition process and destroy the recognizer.
     */
    public void stopVoiceRecognition()
    {
        speechTimeout.cancel();
        if (speech != null) {
            speech.destroy();
            speech = null;
        }
    }

/* RecognitionListener interface implementation */

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d("sss","onReadyForSpeech");

        // create and schedule the input speech timeout
        speechTimeout = new Timer();
        speechTimeout.schedule(new SilenceTimer(), 3000);
        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("sss","onBeginningOfSpeech");

        // Cancel the timeout because voice is arriving
        if (listeningOfSpeechListener != null)
            listeningOfSpeechListener.onListening(true);
        speechTimeout.cancel();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("sss","onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        if (listeningOfSpeechListener != null)
            listeningOfSpeechListener.onListening(false);
        Log.d("sss","onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        String message;
        boolean restart = true;
        switch (error)
        {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                restart = false;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                restart = false;
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

        if (restart) {
            ((PocketAccounter)context).runOnUiThread(new Runnable() {
                public void run() {
                    getSpeechRevognizer().cancel();
                    startVoiceRecognitionCycle();
                }
            });
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d("sss","onEvent");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("sss","onPartialResults");
    }

    @Override
    public void onResults(Bundle results) {
        // Restart new dictation cycle
        startVoiceRecognitionCycle();
        //
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

    public void setListeningOfSpeechListener(ListeningOfSpeechListener listeningOfSpeechListener) {
        this.listeningOfSpeechListener = listeningOfSpeechListener;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//		Log.d("sss","onRmsChanged "+rmsdB);
    }
}
