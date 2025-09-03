package se.disabledsecurity.xkcd.fetcher.functions;

import io.vavr.Function3;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Option;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

public class Functions {
    public static  IntFunction<IntPredicate> notEquals = value -> i -> i != value;
    public static ToIntFunction<String> toIntUnboxed = Integer::parseInt;
    public static ToIntFunction<Integer> toUnboxed = Integer::intValue;

    public static Function3<String, String, String, java.time.LocalDate> toDate =
            (y, m, d) -> LocalDate.of(Functions.toIntUnboxed.applyAsInt(y),
                    Functions.toIntUnboxed.applyAsInt(m),
                    Functions.toIntUnboxed.applyAsInt(d)
            );
    public static Function<String, Either<Throwable, URL>> toUrl = urlString -> Option.of(urlString)
            .<Throwable>toEither(new IllegalArgumentException("URL string cannot be null"))
            .flatMap(str -> Try.of(() -> URI.create(str).toURL()).toEither());

    public static Function<String, Pattern> compilePattern = Pattern::compile;


    private Functions() {
    }


}