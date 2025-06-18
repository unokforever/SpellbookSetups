package com.spellbooksetups;

import lombok.Data;

@Data
public class SpellData
{
    private int spellId;
    private int relativeX;
    private int relativeY;
    private int originalX;
    private int originalY;
    private boolean hidden;
}