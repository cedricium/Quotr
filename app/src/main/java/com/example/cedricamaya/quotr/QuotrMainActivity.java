package com.example.cedricamaya.quotr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
//    int count = 0;

    private TextView quoteText;
    private TextView personText;

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
        personText = (TextView) findViewById(R.id.person);

        // quotes from first example; were hard-coded into the client as shown below
//        final ArrayList<Quote> quoteList = new ArrayList<Quote>();
//
//        Quote quote1 = new Quote("Cool Beans.", "Rod Kimble");
//        quoteList.add(quote1);
//
//        Quote quote2 = new Quote("How can mirrors be real if our eyes " +
//                "aren't real.","Jaden Smith");
//        quoteList.add(quote2);
//
//        Quote quote3 = new Quote("That's like me blaming owls for how bad" +
//                " I suck at analogies.", "Britta Perry");
//        quoteList.add(quote3);
//
//        Quote quote4 = new Quote("You're more of a fun vampire. You don't" +
//                " suck blood, you just suck.", "Troy Barnes");
//        quoteList.add(quote4);
//
//        Quote quote5 = new Quote("You know what's better than materialistic" +
//                " things, knowledge!", "Tai Lopez");
//        quoteList.add(quote5);

        touch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // old code for fetching hard-coded quotes from ArrayList
//                if (count < quoteList.size()) {
//                    Quote q = quoteList.get(count);
//
//                    quoteText.setText("\"" + q.getQuote() + "\"");
//                    personText.setText("-" + q.getPerson());
//                    count++;
//                } else{
//                    count = 0;
//                }

                // every time the screen is pressed, a request for the passed URL
                // is made which contains the JSON data for a random quote
                new JSONTask().execute("http://quotesondesign.com/wp-json/posts" +
                        "?filter[orderby]=rand&filter[posts_per_page]=1");
            }
        });
    }

    // thanks to this tutorial: https://youtu.be/X2mY3zfAMfM
    public class JSONTask extends AsyncTask<String, String, String[]>{
        static final int MAX_QUOTE_LENGTH = 360;
        static final String QUOTE_DASH = "— ";
        static final int INDEX_OF_QUOTE = 0;
        static final int INDEX_OF_AUTHOR = 1;

        @Override
        protected String[] doInBackground(String... params){
            // initalize connection for making HTTP requests and buffer reader
            // for reading data coming from said connection
            HttpURLConnection connection = null;
            BufferedReader reader = null;

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
                while ((line = reader.readLine()) != null){
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
                String quote = finalObject.getString("content");
                String author = finalObject.getString("title");

              //// check to see if quote will overflow out of container -----
                // currently doesn't work as expected: long quote appears for
                // a second then goes displays a new quote - NEED-TO-FIX
                if (quote.length() > MAX_QUOTE_LENGTH)
                    new JSONTask().execute("http://quotesondesign.com/wp-json/posts" +
                            "?filter[orderby]=rand&filter[posts_per_page]=1");

                // an array of two strings is created, first string is the
                // quote, second string is the author
                String[] quoteCombo = {quote, author};

                // send quote and author strings to be cleaned of HTML escape
                // sequences
                quoteCombo = cleanupStrings(quoteCombo);

                return quoteCombo;

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
            return null;
        }

        // method that takes quoteCombo array and parses both strings of HTML
        // escape sequences and returns cleaned-up strings
        private String[] cleanupStrings(String[] quoteAndAuthor){
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
        protected void onPostExecute(String[] quotes){
            super.onPostExecute(quotes);

            // the TextView object that is responsible for displaying the quotes
            // is set to the first element of the the received array
            quoteText.setText(quotes[0]);

            // the TextView object that is responsible for displaying the author
            // is set to the second element of the received array
            personText.setText(QUOTE_DASH + quotes[1]);
        }
    }
}