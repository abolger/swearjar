package com.notenoughawesome.imu;

        import java.io.IOException;
        import java.net.MalformedURLException;
        import java.net.URLConnection;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Locale;
        import java.net.URL;
        import java.io.BufferedInputStream;
        import java.io.InputStream;

        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.AsyncTask;
        import android.speech.RecognizerIntent;
        import android.view.Menu;
        import android.view.View;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;




public class MainActivity extends Activity {
    private String[] curseword_blacklist = { "damn", "dammit", "damnit",
            "arse", "arselicker", "ass", "ass master", "ass kisser", "ass nugget", "ass wipe", "asshole", "bastard", "biest", "bitch", "butt", "butthead", "clit", "cock", "cock master", "cock up", "cockboy", "cockfucker", "cunt", "dogshit", "fart", "fuck", "fuck face", "fuck head", "fuck noggin", "fucker", "jackass", "motherfucker", "porno", "porn", "prick", "retard", "shit eater", "shithead", "shit", "slut", "son of a bitch", "bitch", "whore"
    };
    private String[] buzzword_blacklist = {
            "i o t", "platform", "hack", "hackster", "robot", "arduino", "cloud", "internet of things", "leverage", "-duino", "synergy", "venture capital", "maker", "hacker", "rockstar", "ninja"};

    private ArrayList<String> blackList = new ArrayList<String>();

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        blackList.addAll(Arrays.asList(buzzword_blacklist));
        blackList.addAll(Arrays.asList(curseword_blacklist));


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);



        // hide the action bar
        try {
            getActionBar().hide();
        } catch (NullPointerException e) {
            //Don't worry about it, we don't even have an action bar.
        }

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak now!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry, your phone doesn't support text-to-speech!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<String> usedSwears = new ArrayList<String>();
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String bestResult = result.get(0);
                    int swears = 0;
                    for (String s : blackList) {
                        if (bestResult.toLowerCase().contains(s)) {
                            swears++;
                            usedSwears.add(s);

                        }
                    }
                    if (swears > 0) {
                        String output = "";
                        for (String s2: usedSwears){
                            output = output + "\""+s2.toUpperCase()+"\" ?\n";
                        }

                        txtSpeechInput.setText(output + "Such language!\n You owe $"+swears+ " to the SwearJar!");
                        DownloadWebPageTask task = new DownloadWebPageTask();
                        task.execute("http://10.3.1.142/openJar");
                    } else {
                        txtSpeechInput.setText("Keep on keeping it clean!");
                    }

                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            InputStream in;
            try {
                URL url = new URL(
                        urls[0]);
                URLConnection connector = url.openConnection();
                in = new BufferedInputStream(connector.getInputStream());

                in.read();

                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }


}
