package io.github.admiralxy.cache.jpa.demosb3.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@RequiredArgsConstructor
public class Holder {
    private final int id;
    private final String value;
}
