package ca.bc.gov.educ.api.gradstudent.exception;

import ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class EntityNotFoundException extends GradBusinessRuleException {


    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Entity not found exception.
     *
     * @param clazz           the clazz
     * @param searchParamsMap the search params map
     */
    public EntityNotFoundException(Class clazz, String... searchParamsMap) {
        super(EntityNotFoundException.generateMessage(clazz.getSimpleName(), toMap(String.class, String.class, searchParamsMap)));
    }

    /**
     * Generate message string.
     *
     * @param entity       the entity
     * @param searchParams the search params
     * @return the string
     */
    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity) +
                " was not found for parameters " +
                searchParams;
    }

    /**
     * To map map.
     *
     * @param <K>       the type parameter
     * @param <V>       the type parameter
     * @param keyType   the key type
     * @param valueType the value type
     * @param entries   the entries
     * @return the map
     */
    private static <K, V> Map<K, V> toMap(
            Class<K> keyType, Class<V> valueType, Object... entries) {
        if (entries.length % 2 == 1)
            throw new IllegalArgumentException("Invalid entries");
        return IntStream.range(0, entries.length / 2).map(i -> i * 2)
                .collect(HashMap::new,
                        (m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])),
                        Map::putAll);
    }

}
