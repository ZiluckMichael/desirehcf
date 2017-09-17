package com.desiremc.hcf.scoreboard.common;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.desiremc.hcf.scoreboard.type.Entry;

/**
 * An utility to make pretty entries for the scoreboards, without calculating
 * the positions by yourself.
 *
 * @author TigerHix
 */
public final class EntryBuilder
{

    private final LinkedList<Entry> entries = new LinkedList<>();

    /**
     * Append a blank line.
     *
     * @return this
     */
    public EntryBuilder blank()
    {
        return next("");
    }

    /**
     * Append a new line with specified text.
     *
     * @param string
     *            text
     * @return this
     */
    public EntryBuilder next(String string)
    {
        entries.add(new Entry(adapt(string), entries.size()));
        return this;
    }

    /**
     * Returns a map of entries.
     *
     * @return map
     */
    public List<Entry> build()
    {
        for (Entry entry : entries)
        {
            entry.setPosition(entries.size() - entry.getPosition());
        }
        return entries;
    }

    private String adapt(String entry)
    {
        // Cut off the exceeded part if needed
        if (entry.length() > 48) entry = entry.substring(0, 47);
        return Strings.format(entry);
    }

    public static List<Entry> build(Collection<String> strings)
    {
        EntryBuilder builder = new EntryBuilder();
        int i = 0;
        for (String str : strings)
        {
            builder.next(str);
            i++;
            if (i != strings.size())
            {
                builder.blank();
            }
        }
        return builder.build();
    }

}
