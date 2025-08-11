package se.disabledsecurity.xkcd.fetcher.functions;

import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Option;

import java.net.URI;
import java.net.URL;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public class Functions {
    public static final IntFunction<IntPredicate> notEquals = value -> i -> i != value;

    public static ToIntFunction<String> toIntUnboxed = Integer::parseInt;

    public static Function<String, Either<Throwable, URL>> toUrl = urlString -> Option.of(urlString)
            .<Throwable>toEither(new IllegalArgumentException("URL string cannot be null"))
            .flatMap(str -> Try.of(() -> URI.create(str).toURL()).toEither());

    public static Function<URL, Either<Throwable, String>> toUrlString = url -> Option.of(url)
            .<Throwable>toEither(new IllegalArgumentException("URL cannot be null"))
            .map(URL::toString);
}