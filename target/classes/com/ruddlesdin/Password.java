package com.ruddlesdin;

import java.util.Arrays;
import java.util.List;

/**
 * Created by p_ruddlesdin on 23/03/2017.
 */
public class Password {

   List<String> passwords = Arrays.asList("leader");

    public Password() {

    }

    public boolean checkPasswordExists(String password) {
        if(passwords.contains(password)) {
            return true;
        } else {
            return false;
        }
    }
}
