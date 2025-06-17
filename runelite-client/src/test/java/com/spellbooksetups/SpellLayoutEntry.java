package com.spellbooksetups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpellLayoutEntry
{
    private int position;
    private boolean hidden;
}
