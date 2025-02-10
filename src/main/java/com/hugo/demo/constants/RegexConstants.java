package com.hugo.demo.constants;

import java.util.regex.Pattern;

public class RegexConstants {
    public static final Pattern userIdPattern = Pattern.compile("^[0-9]{1,10}$");
    public static final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$");
    public static final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,16}$");
    public static final Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_-]{6,20}$");
    public static final Pattern pinPattern = Pattern.compile("^[0-9]{4}$");
    public static final Pattern phoneNumberPattern = Pattern.compile("^[0-9]{10}$");
}
