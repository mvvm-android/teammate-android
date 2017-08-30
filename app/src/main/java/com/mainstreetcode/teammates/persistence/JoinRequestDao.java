package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;

import java.util.List;

/**
 * DAO for {@link com.mainstreetcode.teammates.model.JoinRequest}
 */

@Dao
public abstract class JoinRequestDao extends EntityDao<JoinRequestEntity> {

    @Override
    protected String getTableName() {
        return "join_requests";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<JoinRequestEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<JoinRequestEntity> teams);

    @Delete
    public abstract void delete(List<JoinRequestEntity> roles);
}
