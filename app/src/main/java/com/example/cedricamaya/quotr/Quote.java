package com.example.cedricamaya.quotr;

public class Quote {
    public String quote;
    public String author;

    public Quote(String quote, String author){
        super();
        this.quote = quote;
        this.author = author;
    }

    public String getQuote() {
        return quote;
    }
    public String getAuthor() {
        return author;
    }
}