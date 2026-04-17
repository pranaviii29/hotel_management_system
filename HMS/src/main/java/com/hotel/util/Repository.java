package com.hotel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Generics - mandatory Java concept
// Generic repository for any type T
public class Repository<T> {

    private final List<T> items;  // Collection Framework

    public Repository() {
        this.items = new ArrayList<>();
    }

    public void add(T item) {
        items.add(item);
    }

    public void remove(T item) {
        items.remove(item);
    }

    public List<T> getAll() {
        return new ArrayList<>(items);  // defensive copy
    }

    // Generic method using Predicate
    public List<T> filter(Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public Optional<T> findFirst(Predicate<T> predicate) {
        return items.stream().filter(predicate).findFirst();
    }

    public int size() { return items.size(); }

    public void clear() { items.clear(); }

    public boolean contains(T item) { return items.contains(item); }
}
