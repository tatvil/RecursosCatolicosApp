package es.tatvil.recursoscatolicos.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class RosarioAudioManager {

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private int currentAudioResId = 0;
    private Context context; // Necesitamos el contexto para crear MediaPlayer

    // Callback para notificar cuando el audio ha terminado
    public interface AudioCompletionListener {
        void onAudioCompleted();
    }
    private AudioCompletionListener completionListener;

    public RosarioAudioManager(Context context) {
        this.context = context;
    }

    public void setAudioCompletionListener(AudioCompletionListener listener) {
        this.completionListener = listener;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Reproduce un audio dado su ID de recurso.
     * Detiene el audio actual si ya se está reproduciendo.
     * @param audioResId El ID del recurso de audio (ej. R.raw.nombre_del_audio)
     */
    public void playAudio(int audioResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (audioResId != 0) { // Solo si hay un ID de recurso válido
            mediaPlayer = MediaPlayer.create(context, audioResId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                isPlaying = true;
                currentAudioResId = audioResId;

                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    if (completionListener != null) {
                        completionListener.onAudioCompleted();
                    }
                    // Opcional: liberar mediaPlayer inmediatamente después de terminar si no se reutiliza
                    // mediaPlayer.release();
                    // mediaPlayer = null;
                });
            } else {
                Log.e("RosarioAudioManager", "No se pudo crear MediaPlayer para el recurso: " + context.getResources().getResourceEntryName(audioResId));
                isPlaying = false;
            }
        } else {
            Log.d("RosarioAudioManager", "No hay audio para reproducir (audioResId es 0). Deteniendo si hay uno.");
            stopAudio(); // Asegurarse de que no haya un audio anterior sonando sin control
        }
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentAudioResId = 0;
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resumeAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * Libera los recursos del MediaPlayer cuando ya no se necesitan.
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}