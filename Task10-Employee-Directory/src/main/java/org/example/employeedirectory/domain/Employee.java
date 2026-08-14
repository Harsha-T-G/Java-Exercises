package org.example.employeedirectory.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record Employee(String employeeId, String name, String department, Set<String> skills) {

    public Employee {
        Set<String> skillCopy = skills == null ? new HashSet<>() : new HashSet<>(skills);
        skills = Collections.unmodifiableSet(skillCopy);
    }
}
