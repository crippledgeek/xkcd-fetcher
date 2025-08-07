package se.disabledsecurity.xkcd.fetcher.external.model;

public record Xkcd(String month, String link, String year, String news, String safe_title,
                   String transcript, String alt, String title,
                   String day, int num, String img) {}