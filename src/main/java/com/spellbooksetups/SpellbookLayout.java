package com.spellbooksetups;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SpellbookLayout
{
    private List<SpellData> spells = new ArrayList<>();
    private long timestamp = System.currentTimeMillis();
    private String version = "1.0";
}