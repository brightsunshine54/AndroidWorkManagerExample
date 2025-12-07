package com.filantrop.androidworkmanagerexample.sni;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

@Entity(tableName = "sni_table", indices = {@Index(value = {"sni"}, unique = true)})
public class SniDto {
    @Getter
    @Setter
    @PrimaryKey(autoGenerate = true)
    private int id;
    @Getter
    private final String sni;
    @Getter
    @Setter
    private boolean checked = false;

    public SniDto(String sni) {
        this.sni = sni;
    }
}
