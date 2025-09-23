package se.disabledsecurity.xkcd.fetcher.internal.model;


import java.net.URL;
import java.time.LocalDate;

public record Comic(String news, String safe_title,
                     String transcript, String alt, String title,
                     int comicNumber, LocalDate publicationDate, URL imageUrl) {
}