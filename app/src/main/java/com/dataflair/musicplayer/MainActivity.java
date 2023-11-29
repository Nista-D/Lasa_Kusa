package com.dataflair.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dataflair.musicplayer.Activities.PlayerActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

//import org.apache.commons.text.similarity.LevenshteinDistance;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] items;
    String name;
    SearchView searchView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> allSongs;
    private ArrayList<String> displayedSongs;
    private MediaPlayer mediaPlayer;
    private static final int MIN_SIMILARITY_SCORE = 80; // can adjust this threshold as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.ListView);
        searchView = findViewById(R.id.searchView);
        mediaPlayer = new MediaPlayer(); // Initialize MediaPlayer
        allSongs = new ArrayList<>();
        displayedSongs = new ArrayList<>();
        allSongs = displaySong();
        runTimePermission();

        allSongs = new ArrayList<>();
        displayedSongs = new ArrayList<>();
        allSongs = displaySong();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                name = query;
                filterSongs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return false;
            }
        });
    }


    private void filterSongs(String query) {
        displayedSongs.clear();

        for (String song : allSongs) {
            int similarityScore = FuzzySearch.partialRatio(query.trim().toLowerCase(), song.trim().toLowerCase());
            int distance = levenshteinDistance(query.toLowerCase(), song.toLowerCase());

            if (similarityScore >= MIN_SIMILARITY_SCORE || distance <= 2) {
                displayedSongs.add(song);
            }

        }
        updateAdapter();
    }


    private void updateAdapter(){
        // Create a HashSet to store unique songs
        HashSet<String> uniqueSongs = new HashSet<>(displayedSongs);
        displayedSongs.clear();
        displayedSongs.addAll(uniqueSongs);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedSongs);
        listView.setAdapter(adapter);

        // Set a click listener for the filtered list items to play the selected song
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedSongName = displayedSongs.get(i);
            playSong(selectedSongName);

        });
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + costOfSubstitution(s1.charAt(i - 1), s2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }


    private void playSong(String songName) {
        // Find the file path for the selected song
        File songFile = findSongFileByName(songName);
        if (songFile != null) {
            try {
                mediaPlayer.reset(); // Reset MediaPlayer to its idle state
                mediaPlayer.setDataSource(songFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start(); // Start playing the song
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Add this method to find the file by song name
    private File findSongFileByName(String songName) {
        ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        for (File file : mySongs) {
            String fileName = file.getName().replace(".mp3", "");
            if (fileName.equalsIgnoreCase(songName)) {
                return file;
            }
        }
        return null;
    }

    public void runTimePermission() {
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));

                } else {
                    if (singleFile.getName().endsWith(".mp3")) {
                        arrayList.add(singleFile);

                        String songName = singleFile.getName().replace(".mp3", "");
                        allSongs.add(songName);
                    }
                }
            }
        }

        return arrayList;
    }

    public ArrayList<String> displaySong() {
        allSongs = new ArrayList<>();
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];

        for (int i = 0; i < mySongs.size(); i++) {
            String songName = mySongs.get(i).getName().replace(".mp3", ""); // Remove ".mp3" extension
            items[i] = songName;
            allSongs.add(songName); // Add song name to the list
        }

        // Initialize the adapter with all songs
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = allSongs.get(i); // Get the song name from allSongs
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("songs", mySongs);
                intent.putExtra("songname", songName);
                intent.putExtra("pos", i);
                startActivity(intent);
            }
        });
        return allSongs;
    }

//    class CustomAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return items.length;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = getLayoutInflater().inflate(R.layout.song_name_layout, null);
//            TextView txtSong = view.findViewById(R.id.SongName);
//            txtSong.setSelected(true);
//            txtSong.setText(items[position]);
//            return view;
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}