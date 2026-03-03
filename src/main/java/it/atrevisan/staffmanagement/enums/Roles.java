package it.atrevisan.staffmanagement.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Roles {
    ADMIN,
    HR,
    STAFF;

    public static Set<String> toSet(Roles... roles){
        Set<String> result = new HashSet<>();
        for(Roles r : roles){
            result.add(String.valueOf(r));
        }
        return result;
    }
}
