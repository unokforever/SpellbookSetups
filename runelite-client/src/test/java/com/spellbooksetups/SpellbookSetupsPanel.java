package com.spellbooksetups;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.runelite.client.ui.PluginPanel;

public class SpellbookSetupsPanel extends PluginPanel
{
    private final SpellbookSetupsPlugin plugin;
    private final JPanel layoutPanel = new JPanel(new GridLayout(0, 1, 0, 5));

    @Inject
    public SpellbookSetupsPanel(SpellbookSetupsPlugin plugin)
    {
        this.plugin = plugin;
        setLayout(new BorderLayout());

        JButton save = new JButton("Save Current Layout");
        save.addActionListener(e -> onSave());
        add(save, BorderLayout.NORTH);
        add(new JScrollPane(layoutPanel), BorderLayout.CENTER);
    }

    private void onSave()
    {
        String name = JOptionPane.showInputDialog(this, "Layout name");
        if (name != null && !name.isEmpty())
        {
            plugin.saveLayout(name.trim());
        }
    }

    public void rebuild(List<String> layoutNames)
    {
        layoutPanel.removeAll();
        for (String name : layoutNames)
        {
            JButton b = new JButton(name);
            b.addActionListener(e -> plugin.applyLayout(name));
            layoutPanel.add(b);
        }
        revalidate();
        repaint();
    }
}
