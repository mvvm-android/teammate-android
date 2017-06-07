package com.mainstreetcode.teammates.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Roles on a team
 * <p>
 * Created by Shemanigans on 6/6/17.
 */

@Getter
@Setter
public class Role {

    public static final String DB_NAME = "roles";
    public static final String SEARCH_INDEX_KEY = "name";

    boolean isEditor;
    String id;
    String name;

    public Role(String id, DataSnapshot snapshot) {
        Map<String, Object> data = snapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
        this.id = id;
        this.name = (String) data.get("name");
    }
}
