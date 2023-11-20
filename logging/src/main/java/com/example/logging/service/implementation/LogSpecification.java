package com.example.logging.service.implementation;

import com.example.logging.exception.IllegalTypeException;
import com.example.logging.model.Level;
import com.example.logging.model.Log;
import com.example.logging.model.Metadata;
import com.example.logging.model.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LogSpecification {

    private final static Map<String, Field> logFields;

    static {

        Map<String, Field> m = new HashMap<>();

        // The field map is hard coded here. This needs to be dynamic.
        for (Field field : Log.class.getDeclaredFields()) {
            if (field.getName().equals("metadata")) {

                for (Field field_ : Metadata.class.getDeclaredFields()) {
                    if (field_.getName().equals("parentResourceId")) {

                        m.put("metadata.parentResourceId", field_);
                        break;
                    }
                }
            } else {
                m.put(field.getName(), field);
            }
        }

        logFields = m;
    }

    private static String modifySearchStr(String s) {

        String[] strings = s.split("");

        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals("\\") || strings[i].equals("%") || strings[i].equals("_")) {
                strings[i] = '\\' + strings[i];
            }
        }

        return "%" + String.join("%", strings) + "%";
    }

    public static Specification<Log> inTimestampRange(Instant past, Instant future) {

        Assert.isTrue(past == null ^ future != null, "Both past and future instance should be either null or a instance object");

        if (past != null) {

            Assert.isTrue(future.compareTo(past) >= 0, "Past instance cannot be after the future instance");

            return (root, query, builder) -> builder.between(root.get("timestamp"), past, future);
        }

        return (root, query, builder) -> builder.conjunction();
    }

    public static Specification<Log> filterByRole(Role role, Map<Role, Level> logLevelPermissions) {

        Assert.notNull(logLevelPermissions, "Log level permissions map cannot be null");

        return (root, query, builder) -> {

            if (role == null) {
                return builder.conjunction();
            }

            Predicate predicate = builder.disjunction();
            List<Level> hierarchy = Level.hierarchy;
            Level level = logLevelPermissions.get(role);

            if (level == null) {
                throw new RuntimeException("Invalid role: " + role.getName());
            }

            boolean b = false;
            for (Level l : hierarchy) {

                if (l.equals(level)) {
                    b = true;
                }

                if (b) {
                    predicate = builder.or(predicate, builder.equal(root.get("level"), l));
                }
            }

            return predicate;
        };
    }

    public static Specification<Log> filterByRoles(List<Role> roles, Map<Role, Level> logLevelPermissions) {

        Assert.notNull(logLevelPermissions, "Log level permissions map cannot be null");

        return (root, query, builder) -> {

            Predicate predicate = builder.disjunction();

            for (Role role : roles) {
                Specification<Log> spec = filterByRole(role, logLevelPermissions);
                predicate = builder.or(predicate, spec.toPredicate(root, query, builder));
            }

            return predicate;
        };
    }

    public static Specification<Log> filterByProperties(Map<String, Object> filters) {

        return (root, query, builder) -> {

            Predicate predicate = builder.conjunction();

            if (filters == null) {
                return predicate;
            }

            for (Map.Entry<String, Object> entry : filters.entrySet()) {

                String json = entry.getKey();
                Object value = entry.getValue();
                Field field = logFields.get(json);

                if (!field.getType().isAssignableFrom(value.getClass())) {
                    throw new IllegalTypeException("The provided value cannot be assigned to field: " + json);
                }

                // This nested field-specific operation is hard coded. Need to be dynamic.
                if (field.getName().equals("parentResourceId")) {
                    Join<Log, Metadata> join = root.join("metadata");
                    predicate = builder.and(predicate, builder.equal(join.get("parentResourceId"), value));
                } else {
                    predicate = builder.and(predicate, builder.equal(root.get(field.getName()), value));
                }
            }

            return predicate;
        };
    }

    public static Specification<Log> search(String searchStr) {

        return (root, query, builder) -> {

            if (searchStr == null) {
                return builder.conjunction();
            }

            Predicate predicate = builder.disjunction();
            Join<Log, Metadata> joined = root.join("metadata");
            String ss = modifySearchStr(searchStr);

            for (Field field : Log.class.getDeclaredFields()) {
                if (field.getType().equals(String.class) || field.getType().isEnum()) {
                    Predicate like = builder.like(builder.lower(root.get(field.getName())), builder.literal(ss.toLowerCase()), '\\');
                    predicate = builder.or(predicate, like);
                }
            }
            for (Field field : Metadata.class.getDeclaredFields()) {
                if (field.getType().equals(String.class) || field.getType().isEnum()) {
                    Predicate like = builder.like(builder.lower(joined.get(field.getName())), builder.literal(ss.toLowerCase()), '\\');
                    predicate = builder.or(predicate, like);
                }
            }

            return predicate;
        };
    }
}
