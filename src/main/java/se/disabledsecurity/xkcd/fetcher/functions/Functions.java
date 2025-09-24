package se.disabledsecurity.xkcd.fetcher.functions;

import io.vavr.Function3;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

public final class Functions {

    public static final IntFunction<IntPredicate> NOT_EQUALS = value -> i -> i != value;
    public static final ToIntFunction<String> TO_INT_UNBOXED = Integer::parseInt;
    public static final ToIntFunction<Integer> TO_UNBOXED = Integer::intValue;

    public static final Function3<String, String, String, LocalDate> TO_DATE =
            (y, m, d) -> LocalDate.of(
                    TO_INT_UNBOXED.applyAsInt(y),
                    TO_INT_UNBOXED.applyAsInt(m),
                    TO_INT_UNBOXED.applyAsInt(d)
            );

    public static final Function<String, Either<Throwable, URL>> TO_URL = urlString ->
            Option.of(urlString)
                    .<Throwable>toEither(new IllegalArgumentException("URL string cannot be null"))
                    .flatMap(str -> Try.of(() -> URI.create(str).toURL()).toEither());

    public static final Function<String, Pattern> COMPILE_PATTERN = Pattern::compile;

    private Functions() {
    }

    /** General extractor: tries markers in order; falls back to last path segment. */
    public static Optional<String> extractAfterMarkers(String u, String... markers) {
        if (u == null || u.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(markers)
                .filter(marker -> marker != null && !marker.isBlank())
                .map(marker -> extractAfterMarker(u, marker))
                .filter(s -> !s.isEmpty())
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> {
                    String last = extractLastSegment(u);
                    return last.isEmpty() ? Optional.empty() : Optional.of(last);
                });
    }

    /** Convenience for your current need: only "/comics/". */
    public static Optional<String> extractComicsFileName(String url) {
        return extractAfterMarkers(url, "/comics/");
    }

    // --- helpers ---
    private static String extractAfterMarker(String u, String marker) {
        int idx = u.indexOf(marker);
        if (idx < 0) {
            return "";
        }
        int start = idx + marker.length();
        return (start < u.length()) ? u.substring(start).replaceFirst("^/+", "") : "";
    }

    private static String extractLastSegment(String u) {
        int idx = u.lastIndexOf('/') + 1;
        return (idx <= 0 || idx >= u.length()) ? "" : u.substring(idx);
    }

    public static <T> Predicate<T> logOnFalse(Predicate<T> base,
                                              Logger log,
                                              Function<T, String> messageFn) {
        return t -> {
            boolean result = base.test(t);
            if (!result) {
                log.info(messageFn.apply(t));
            }
            return result;
        };
    }

    /** Decorate a predicate to log when it returns true. (Handy sometimes.) */
    public static <T> Predicate<T> logOnTrue(Predicate<T> base,
                                             Logger log,
                                             Function<T, String> messageFn) {
        return t -> {
            boolean result = base.test(t);
            if (result) {
                log.info(messageFn.apply(t));
            }
            return result;
        };
    }

    /** HOC: build a "keep if NOT excluded" predicate with logging. */
    public static Function<Set<Integer>, Predicate<Integer>> excludeComics(Logger log) {
        return excluded -> logOnFalse(
                id -> !excluded.contains(id),                 // keep
                log,
                id -> "Skipping excluded comic " + id         // message
        );
    }
}
