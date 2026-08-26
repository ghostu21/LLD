package com.lld.patterns.builder.student;

import java.util.ArrayList;
import java.util.List;

public class MBAStudentBuilder extends StudentBuilder {
    @Override
    public StudentBuilder setSubjects() {
        List<String> mbaSubjectsList = new ArrayList<>();
        mbaSubjectsList.add("Micro Economics");
        mbaSubjectsList.add("Business Studies");
        mbaSubjectsList.add("Operations Management");
        mbaSubjectsList.add("Financial Management");
        this.subjects = mbaSubjectsList;
        return this;
    }
}
