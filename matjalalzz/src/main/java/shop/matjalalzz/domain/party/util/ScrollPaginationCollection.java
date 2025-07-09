package shop.matjalalzz.domain.party.util;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScrollPaginationCollection<T> {

    private final List<T> currentScrollItems;
    private final boolean lastScroll;

    public static <T> ScrollPaginationCollection<T> of(List<T> fetchedItems, int size) {
        boolean isLast = fetchedItems.size() <= size;
        List<T> current = isLast ? fetchedItems : fetchedItems.subList(0, size);
        return new ScrollPaginationCollection<>(current, isLast);
    }

    public T getNextCursor() {
        if (lastScroll || currentScrollItems.isEmpty()) {
            return null;
        }
        return currentScrollItems.get(currentScrollItems.size() - 1);
    }

    public boolean isLastScroll() {
        return lastScroll;
    }

}
