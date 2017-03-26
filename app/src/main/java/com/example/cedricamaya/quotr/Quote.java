package com.example.cedricamaya.quotr;

public class Quote {
    public String quote;
    public String person;

    public Quote(String quote, String person){
        super();
        this.quote = quote;
        this.person = person;
    }

    public String getPerson() {
        return person;
    }

    public String getQuote() {
        return quote;
    }
}
