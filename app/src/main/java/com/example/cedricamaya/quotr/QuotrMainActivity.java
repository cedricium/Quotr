package com.example.cedricamaya.quotr;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class QuotrMainActivity extends AppCompatActivity {
    private TextView quoteText;
    private TextView authorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotr_main);

        RelativeLayout touch = (RelativeLayout) findViewById(R.id.touch);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        quoteText = (TextView) findViewById(R.id.quote);
        authorText = (TextView) findViewById(R.id.person);

        touch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // every time the screen is pressed, a request for the passed URL
            // is made which contains the JSON data for a random quote
                if(isNetworkAvailable(getApplicationContext())) {
                    new JSONTask().execute("http://quotesondesign.com/wp-json/posts" +
                            "?filter[orderby]=rand&filter[posts_per_page]=1");
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Internet Not Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }

        return false;
    }

    // thanks to this tutorial: https://youtu.be/X2mY3zfAMfM
    public class JSONTask extends AsyncTask<String, String, String[]>{
        // int constants for the length of each screen size
        static final int MAX_QUOTE_LENGTH_LARGE = 1000;        // large screen
        static final int MAX_QUOTE_LENGTH_NORMAL = 350;        // normal screen
        static final int MAX_QUOTE_LENGTH_SMALL = 200;         // small screen

        static final String DEFAULT_QUOTE = "(undefined)";
        static final String DEFAULT_AUTHOR = "(undefined)";

        static final String QUOTE_DASH = "— ";
        static final int INDEX_OF_QUOTE = 0;
        static final int INDEX_OF_AUTHOR = 1;

        @Override
        protected String[] doInBackground(String... params) {
            char screenSize;

            //// determine screen size
            if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK)
                    == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                screenSize = 'L';
            } else if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK)
                    == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
                screenSize = 'N';
            } else if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK)
                    == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                screenSize = 'S';
            } else {
                screenSize = 'X';
            }

            // initialize connection for making HTTP requests and buffer reader
            // for reading data coming from said connection
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            boolean quoteMatchesScreen = false;
            String quote = DEFAULT_QUOTE;
            String author = DEFAULT_AUTHOR;

            while (!quoteMatchesScreen) {
                try {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    // reader goes through each line of JSON from the given URL and
                    // adds it line-by-line to buffer
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    // the JSON from the HTTP request is now saved as a String which
                    // can be turned into an actual JSON object
                    String finalJSON = buffer.toString();

                    // parsing finalJSON (the String) to turn into an actual JSON
                    // object
                    JSONArray parentArray = new JSONArray(finalJSON);
                    JSONObject finalObject = parentArray.getJSONObject(0);

                    // once JSON object is established, we can target specific
                    // identifiers and values, such as the quote and its author
                    quote = finalObject.getString("content");
                    author = finalObject.getString("title");

                    if (screenSize == 'S' && quote.length() <= MAX_QUOTE_LENGTH_SMALL) {
                        quoteMatchesScreen = true;
                        break;
                    }
                    if (screenSize == 'N' && quote.length() <= MAX_QUOTE_LENGTH_NORMAL) {
                        quoteMatchesScreen = true;
                        break;
                    }
                    if (screenSize == 'L' && quote.length() <= MAX_QUOTE_LENGTH_LARGE) {
                        quoteMatchesScreen = true;
                        break;
                    }

                    //// test screen size and quote length
                    if (screenSize == 'S' && quote.length() > MAX_QUOTE_LENGTH_SMALL) {
                        continue;
                    }
                    if (screenSize == 'N' && quote.length() > MAX_QUOTE_LENGTH_NORMAL) {
                        continue;
                    }
                    if (screenSize == 'L' && quote.length() > MAX_QUOTE_LENGTH_LARGE) {
                        continue;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null)
                        connection.disconnect();
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // an array of two strings is created, first string is the
            // quote, second string is the author
            String[] quoteCombo = {quote, author};

            // send quote and author strings to be cleaned of HTML escape
            // sequences
            quoteCombo = cleanupStrings(quoteCombo);

            return quoteCombo;
        }

        // method that takes quoteCombo array and parses both strings of HTML
        // escape sequences and returns cleaned-up strings
        private String[] cleanupStrings(String[] quoteAndAuthor) {
            // an array of HTML escape sequences and tags to be replaced
            String[] searchList = {
                    "&#038;",       // &
                    "&#083;",       // &
                    "&#233;",       // é
                    "&#8211;",      // -
                    "&#8212;",      // —
                    "&#8216;",      // '
                    "&#8217;",      // '
                    "&#8220;",      // "
                    "&#8221;",      // "
                    "&#8230;",      // …
                    "&#8243;",      // ″
                    "&hellip;",     // …

                    // HTML tags
                    "<br />",
                    "<p>",
                    "</p>",
                    "<strong>",
                    "</strong>",
                    "<em>",
                    "</em>"
            };

            // an array of characters that replace the above HTML escape
            // sequences and tags
            String[] replacementList = {
                    "&",
                    "&",
                    "é",
                    "–",
                    "—",
                    "\'",
                    "\'",
                    "\"",
                    "\"",
                    "…",
                    "″",
                    "…",

                    // HTML tags
                    "\n",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            };

            // implementation of the replaceEach() method using the above
            // String arrays and quote string
            quoteAndAuthor[INDEX_OF_QUOTE]
                    = StringUtils.replaceEach(quoteAndAuthor[INDEX_OF_QUOTE],
                    searchList, replacementList);
            quoteAndAuthor[INDEX_OF_AUTHOR]
                    = StringUtils.replaceEach(quoteAndAuthor[INDEX_OF_AUTHOR],
                    searchList, replacementList);

            return quoteAndAuthor;
        }

        @Override
        protected void onPostExecute(String[] quotes) {
            super.onPostExecute(quotes);

            // the TextView object that is responsible for displaying the quotes
            // is set to the first element of the the received array
            quoteText.setText(quotes[0]);

            // the TextView object that is responsible for displaying the author
            // is set to the second element of the received array
            authorText.setText(QUOTE_DASH + quotes[1]);
        }
    }
}
