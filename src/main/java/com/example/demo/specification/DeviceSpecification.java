package com.example.demo.specification;

import org.springframework.data.jpa.domain.Specification;


import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;

public class DeviceSpecification {
    
    public static Specification<Device> withFilter(String title, DeviceType type, 
                                                 Double minPower, Double maxPower, Boolean active) {
        return Specification.allOf(titleLike(title))
                .and(typeEqual(type))
                .and(powerBetween(minPower, maxPower))
                .and(activeEqual(active));
    }
    
    private static Specification<Device> titleLike(String title) {
        return (root, query, cb) -> {
            if (title == null) return null;
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }
    
    private static Specification<Device> typeEqual(DeviceType type) {
        return (root, query, cb) -> type != null ? cb.equal(root.get("type"), type) : null;
    }
    
    private static Specification<Device> powerBetween(Double minPower, Double maxPower) {
        return (root, query, cb) -> {
            if (minPower == null && maxPower == null) return null;
            if (minPower != null && maxPower != null) return cb.between(root.get("power"), minPower, maxPower);
            if (minPower != null) return cb.greaterThanOrEqualTo(root.get("power"), minPower);
            return cb.lessThanOrEqualTo(root.get("power"), maxPower);
        };
    }
    
    private static Specification<Device> activeEqual(Boolean active) {
        return (root, query, cb) -> active != null ? cb.equal(root.get("active"), active) : null;
    }
}
