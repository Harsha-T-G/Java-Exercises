package org.example.price;

import java.math.BigDecimal;

/** Supplies one price quote. Implementations may perform blocking work. */
public interface PriceProvider {
    String name();

    BigDecimal fetchPrice() throws Exception;
}
