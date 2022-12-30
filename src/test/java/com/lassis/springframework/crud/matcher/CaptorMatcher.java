package com.lassis.springframework.crud.matcher;

import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CaptorMatcher<T> extends BaseMatcher<T> {
    private final List<Object> objects = new ArrayList<>();
    private final Matcher<T> matcher;

    @Override
    public boolean matches(Object o) {
        objects.add(o);
        return matcher.matches(o);
    }

    @Override
    public void describeTo(Description description) {
        matcher.describeTo(description);
    }

    public List<T> getValues() {
        return objects.stream().map(v -> (T) v)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public Optional<T> getLastValue() {
        return objects.size() > 0 ? Optional.of(objects.get(objects.size() - 1)).map(v -> (T) v) : Optional.empty();
    }
}
