package com.arnesh.interviewsphere.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Tag {

    ARRAY("Array"),
    BACKTRACKING("Backtracking"),
    BICONNECTED_COMPONENT("Biconnected Component"),
    BINARY_INDEXED_TREE("Binary Indexed Tree"),
    BINARY_SEARCH("Binary Search"),
    BINARY_SEARCH_TREE("Binary Search Tree"),
    BINARY_TREE("Binary Tree"),
    BIT_MANIPULATION("Bit Manipulation"),
    BITMASK("Bitmask"),
    BRAINTEASER("Brainteaser"),
    BREADTH_FIRST_SEARCH("Breadth-First Search"),
    BUCKET_SORT("Bucket Sort"),
    COMBINATORICS("Combinatorics"),
    CONCURRENCY("Concurrency"),
    COUNTING("Counting"),
    COUNTING_SORT("Counting Sort"),
    DEPTH_FIRST_SEARCH("Depth-First Search"),
    DIVIDE_AND_CONQUER("Divide and Conquer"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    ENUMERATION("Enumeration"),
    EULERIAN_CIRCUIT("Eulerian Circuit"),
    GAME_THEORY("Game Theory"),
    GEOMETRY("Geometry"),
    GRAPH("Graph"),
    GREEDY("Greedy"),
    HASH_FUNCTION("Hash Function"),
    HASH_TABLE("Hash Table"),
    HEAP_PRIORITY_QUEUE("Heap (Priority Queue)"),
    INTERACTIVE("Interactive"),
    LINE_SWEEP("Line Sweep"),
    LINKED_LIST("Linked List"),
    MATH("Math"),
    MATRIX("Matrix"),
    MEMOIZATION("Memoization"),
    MERGE_SORT("Merge Sort"),
    MINIMUM_SPANNING_TREE("Minimum Spanning Tree"),
    MONOTONIC_QUEUE("Monotonic Queue"),
    MONOTONIC_STACK("Monotonic Stack"),
    NUMBER_THEORY("Number Theory"),
    ORDERED_SET("Ordered Set"),
    PREFIX_SUM("Prefix Sum"),
    PROBABILITY_AND_STATISTICS("Probability and Statistics"),
    QUEUE("Queue"),
    QUICKSELECT("Quickselect"),
    RADIX_SORT("Radix Sort"),
    RANDOMIZED("Randomized"),
    RECURSION("Recursion"),
    ROLLING_HASH("Rolling Hash"),
    SEGMENT_TREE("Segment Tree"),
    SHORTEST_PATH("Shortest Path"),
    SIMULATION("Simulation"),
    SLIDING_WINDOW("Sliding Window"),
    SORTING("Sorting"),
    STACK("Stack"),
    STRING("String"),
    STRING_MATCHING("String Matching"),
    STRONGLY_CONNECTED_COMPONENT("Strongly Connected Component"),
    SUFFIX_ARRAY("Suffix Array"),
    TOPOLOGICAL_SORT("Topological Sort"),
    TREE("Tree"),
    TRIE("Trie"),
    TWO_POINTERS("Two Pointers"),
    UNION_FIND("Union Find");

    @JsonValue
    private final String value;

    Tag(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Tag fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Tag value cannot be null");
        }
        for (Tag tag : Tag.values()) {
            if (tag.value.equalsIgnoreCase(value) || tag.name().equalsIgnoreCase(value)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown tag value: " + value);
    }
}