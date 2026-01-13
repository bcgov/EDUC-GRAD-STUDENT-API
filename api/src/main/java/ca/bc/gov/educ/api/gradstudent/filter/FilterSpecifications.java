package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.function.Function;

@Service
public class FilterSpecifications<E, T extends Comparable<T>> {

    private EnumMap<FilterOperation, Function<FilterCriteria<T>, Specification<E>>> map;

    public FilterSpecifications() {
        initSpecifications();
    }

    public Function<FilterCriteria<T>, Specification<E>> getSpecification(FilterOperation operation) {
        return map.get(operation);
    }

    @PostConstruct
    public void initSpecifications() {

        map = new EnumMap<>(FilterOperation.class);

        // Equal
        map.put(FilterOperation.EQUAL, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");

                if(filterCriteria.getConvertedSingleValue() == null) {
                    // Handle null check for nested properties
                    if(splits.length == 2) {
                        return criteriaBuilder.isNull(root.join(splits[0]).get(splits[1]));
                    } else {
                        return criteriaBuilder.isNull(root.join(splits[0]).get(splits[1]).get(splits[2]));
                    }
                }

                if(splits.length == 2) {
                    return criteriaBuilder.equal(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
                } else {
                    return criteriaBuilder.equal(root.join(splits[0]).get(splits[1]).get(splits[2]), filterCriteria.getConvertedSingleValue());
                }

            } else if(filterCriteria.getConvertedSingleValue() == null) {
                return criteriaBuilder.isNull(root.get(filterCriteria.getFieldName()));
            }
            return criteriaBuilder.equal(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        // Equal using left join
        map.put(FilterOperation.EQUAL_WITH_LEFT_JOIN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");

                Join<Object, Object> join = root.join(splits[0], JoinType.LEFT);
                if (splits.length == 2) {
                    return criteriaBuilder.equal(join.get(splits[1]), filterCriteria.getConvertedSingleValue());
                } else {
                    Join<Object, Object> join2 = join.join(splits[1], JoinType.LEFT);
                    return criteriaBuilder.equal(join2.get(splits[2]), filterCriteria.getConvertedSingleValue());
                }
            } else if (filterCriteria.getConvertedSingleValue() == null) {
                return criteriaBuilder.isNull(root.get(filterCriteria.getFieldName()));
            }
            return criteriaBuilder.equal(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.NOT_EQUAL_OTHER_COLUMN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(",")) {
                String[] splits = filterCriteria.getFieldName().split(",");
                if(splits.length == 2) {
                    return criteriaBuilder.notEqual(root.get(splits[0]), root.get(splits[1]));
                }
            }
            throw new GradStudentAPIRuntimeException("Invalid search criteria provided");
        });

        map.put(FilterOperation.NOT_EQUAL, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");

                if(filterCriteria.getConvertedSingleValue() == null) {
                    // Handle null check for nested properties
                    if(splits.length == 2) {
                        return criteriaBuilder.isNotNull(root.join(splits[0]).get(splits[1]));
                    } else {
                        return criteriaBuilder.isNotNull(root.join(splits[0]).get(splits[1]).get(splits[2]));
                    }
                }

                if(splits.length == 2) {
                    return criteriaBuilder.notEqual(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
                } else {
                    return criteriaBuilder.notEqual(root.join(splits[0]).get(splits[1]).get(splits[2]), filterCriteria.getConvertedSingleValue());
                }
            } else if(filterCriteria.getConvertedSingleValue() == null) {
                return criteriaBuilder.isNotNull(root.get(filterCriteria.getFieldName()));
            }
            return criteriaBuilder.notEqual(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.GREATER_THAN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.greaterThan(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
            }
            return criteriaBuilder.greaterThan(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.GREATER_THAN_OR_EQUAL_TO, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.greaterThanOrEqualTo(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.LESS_THAN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.lessThan(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
            }
            return criteriaBuilder.lessThan(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.LESS_THAN_OR_EQUAL_TO, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.lessThanOrEqualTo(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
            }
           return criteriaBuilder.lessThanOrEqualTo(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
        });

        map.put(FilterOperation.IN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                if(splits.length == 2) {
                    return root.join(splits[0]).get(splits[1]).in(filterCriteria.getConvertedValues());
                } else {
                    return root.join(splits[0]).get(splits[1]).get(splits[2]).in(filterCriteria.getConvertedValues());
                }
            }
            return root.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedValues());
        });

        map.put(FilterOperation.IN_NOT_DISTINCT, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return root.join(splits[0]).get(splits[1]).in(filterCriteria.getConvertedValues());
            }
            return root.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedValues());
        });

        map.put(FilterOperation.NOT_IN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.or(criteriaBuilder.not(root.join(splits[0], JoinType.LEFT).get(splits[1]).in(filterCriteria.getConvertedValues())), criteriaBuilder.isEmpty(root.get(splits[0])));
            }
            return criteriaBuilder.or(criteriaBuilder.not(root.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedValues())), criteriaBuilder.isNull(root.get(filterCriteria.getFieldName())));
        });

        map.put(FilterOperation.BETWEEN,
                filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
                    if (filterCriteria.getFieldName().contains(".")) {
                        String[] splits = filterCriteria.getFieldName().split("\\.");
                        return criteriaBuilder.between(root.join(splits[0]).get(splits[1]), filterCriteria.getMinValue(),
                                filterCriteria.getMaxValue());
                    } else {
                        return criteriaBuilder.between(
                                root.get(filterCriteria.getFieldName()), filterCriteria.getMinValue(),
                                filterCriteria.getMaxValue());
                    }
                });

        map.put(FilterOperation.CONTAINS, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
                .like(root.get(filterCriteria.getFieldName()), "%" + filterCriteria.getConvertedSingleValue() + "%"));

        map.put(FilterOperation.CONTAINS_IGNORE_CASE, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
                .like(criteriaBuilder.lower(root.get(filterCriteria.getFieldName())), "%" + filterCriteria.getConvertedSingleValue().toString().toLowerCase() + "%"));

        map.put(FilterOperation.IN_LEFT_JOIN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                return criteriaBuilder.or(root.join(splits[0], JoinType.LEFT).get(splits[1]).in(filterCriteria.getConvertedValues()));
            }
            return root.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedValues());
        });

        map.put(FilterOperation.CUSTOM_CHILD_JOIN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            if (filterCriteria.getFieldName().contains(".")) {
                String[] splits = filterCriteria.getFieldName().split("\\.");
                if(splits.length == 2) {
                    var incomingFilesetIDVal = root.get("incomingFileset").get("incomingFilesetID");
                    root.join(splits[0], JoinType.LEFT).join(splits[1], JoinType.RIGHT);
                    return criteriaBuilder.equal(root.get("incomingFileset").get("incomingFilesetID"), incomingFilesetIDVal);
                }
            }
            throw new GradStudentAPIRuntimeException("Invalid search criteria provided");
        });

        map.put(FilterOperation.DATE_RANGE, this::dateRangeSpec);

        map.put(FilterOperation.DATE_TIME_RANGE, this::dateRangeSpec);

    }

    private Specification<E> dateRangeSpec(FilterCriteria<T> fc) {
        return (root, query, cb) -> buildDateRangePredicate(fc, root, cb);
    }

    private Predicate buildDateRangePredicate(FilterCriteria<T> fc, Root<E> root, CriteriaBuilder cb) {
        T minValue = fc.getMinValue();
        T maxValue = fc.getMaxValue();

        if (fc.getFieldName().contains(".")) {
            String[] splits = fc.getFieldName().split("\\.");

            if(splits.length == 2) {
                // Both start and end dates provided
                if (minValue != null && maxValue != null) {
                    return cb.between(root.join(splits[0]).get(splits[1]), minValue, maxValue);
                }
                // Only start date provided (>= start)
                else if (minValue != null) {
                    return cb.greaterThanOrEqualTo(root.join(splits[0]).get(splits[1]), minValue);
                }
                // Only end date provided (<= end)
                else if (maxValue != null) {
                    return cb.lessThanOrEqualTo(root.join(splits[0]).get(splits[1]), maxValue);
                }
            } else {
                // 3-level nested property
                if (minValue != null && maxValue != null) {
                    return cb.between(root.join(splits[0]).get(splits[1]).get(splits[2]), minValue, maxValue);
                }
                else if (minValue != null) {
                    return cb.greaterThanOrEqualTo(root.join(splits[0]).get(splits[1]).get(splits[2]), minValue);
                }
                else if (maxValue != null) {
                    return cb.lessThanOrEqualTo(root.join(splits[0]).get(splits[1]).get(splits[2]), maxValue);
                }
            }
        } else {
            // Direct property (no nested join)
            if (minValue != null && maxValue != null) {
                return cb.between(root.get(fc.getFieldName()), minValue, maxValue);
            }
            else if (minValue != null) {
                return cb.greaterThanOrEqualTo(root.get(fc.getFieldName()), minValue);
            }
            else if (maxValue != null) {
                return cb.lessThanOrEqualTo(root.get(fc.getFieldName()), maxValue);
            }
        }

        throw new GradStudentAPIRuntimeException("DATE_RANGE operation requires at least one date value");
    }

}
