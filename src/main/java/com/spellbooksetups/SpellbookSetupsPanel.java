package com.spellbooksetups;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

@Slf4j
public class SpellbookSetupsPanel extends PluginPanel
{
    private final SpellbookSetupsPlugin plugin;
    private final SpellbookLayoutManager layoutManager;
    
    private JTextField layoutNameField;
    private JButton saveButton;
    private JPanel layoutListPanel;
    private JLabel currentLayoutLabel;

    public SpellbookSetupsPanel(SpellbookSetupsPlugin plugin, SpellbookLayoutManager layoutManager)
    {
        this.plugin = plugin;
        this.layoutManager = layoutManager;
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        initComponents();
        refreshLayoutList();
        updateCurrentLayoutDisplay();
    }

    private void initComponents()
    {
        JLabel titleLabel = new JLabel("Spellbook Setups");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        currentLayoutLabel = new JLabel("Current: None");
        currentLayoutLabel.setFont(currentLayoutLabel.getFont().deriveFont(Font.ITALIC));
        currentLayoutLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel savePanel = new JPanel(new BorderLayout(5, 5));
        savePanel.setBorder(BorderFactory.createTitledBorder("Save Current Layout"));
        
        layoutNameField = new JTextField();
        layoutNameField.setToolTipText("Enter layout name");
        
        saveButton = new JButton("Save Layout");
        saveButton.addActionListener(e -> saveCurrentLayout());
        
        savePanel.add(layoutNameField, BorderLayout.CENTER);
        savePanel.add(saveButton, BorderLayout.SOUTH);
        
        JPanel layoutSection = new JPanel(new BorderLayout());
        layoutSection.setBorder(BorderFactory.createTitledBorder("Saved Layouts"));
        
        layoutListPanel = new JPanel();
        layoutListPanel.setLayout(new BoxLayout(layoutListPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(layoutListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        layoutSection.add(scrollPane, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(currentLayoutLabel, BorderLayout.CENTER);
        topPanel.add(savePanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(layoutSection, BorderLayout.CENTER);
    }

    private void saveCurrentLayout()
    {
        String layoutName = layoutNameField.getText().trim();
        if (layoutName.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please enter a layout name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (layoutManager.hasLayout(layoutName))
        {
            int result = JOptionPane.showConfirmDialog(this, 
                "Layout '" + layoutName + "' already exists. Overwrite?", 
                "Confirm Overwrite", 
                JOptionPane.YES_NO_OPTION);
            
            if (result != JOptionPane.YES_OPTION)
            {
                return;
            }
        }
        
        plugin.saveCurrentLayout(layoutName);
        layoutNameField.setText("");
        updateCurrentLayoutDisplay();
    }

    public void refreshLayoutList()
    {
        layoutListPanel.removeAll();
        
        Set<String> layoutNames = layoutManager.getLayoutNames();
        if (layoutNames.isEmpty())
        {
            JLabel noLayoutsLabel = new JLabel("No saved layouts");
            noLayoutsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noLayoutsLabel.setForeground(Color.GRAY);
            layoutListPanel.add(noLayoutsLabel);
        }
        else
        {
            for (String layoutName : layoutNames)
            {
                layoutListPanel.add(createLayoutPanel(layoutName));
                layoutListPanel.add(Box.createVerticalStrut(5));
            }
        }
        
        layoutListPanel.revalidate();
        layoutListPanel.repaint();
    }

    private JPanel createLayoutPanel(String layoutName)
    {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel nameLabel = new JLabel(layoutName);
        nameLabel.setBorder(new EmptyBorder(5, 10, 5, 5));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        
        JButton applyButton = new JButton("Apply");
        applyButton.setPreferredSize(new Dimension(60, 25));
        applyButton.addActionListener(e -> plugin.applyLayout(layoutName));
        
        JButton deleteButton = new JButton("×");
        deleteButton.setPreferredSize(new Dimension(25, 25));
        deleteButton.setForeground(Color.RED);
        deleteButton.setToolTipText("Delete layout");
        deleteButton.addActionListener(e -> deleteLayout(layoutName));
        
        buttonPanel.add(applyButton);
        buttonPanel.add(deleteButton);
        
        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }

    private void deleteLayout(String layoutName)
    {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete layout '" + layoutName + "'?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION)
        {
            plugin.deleteLayout(layoutName);
            updateCurrentLayoutDisplay();
        }
    }

    public void updateCurrentLayoutDisplay()
    {
        String currentLayout = plugin.getLastAppliedLayout();
        if (currentLayout != null && !currentLayout.isEmpty())
        {
            currentLayoutLabel.setText("Current: " + currentLayout);
            currentLayoutLabel.setForeground(new Color(0, 150, 0));
        }
        else
        {
            currentLayoutLabel.setText("Current: None");
            currentLayoutLabel.setForeground(Color.GRAY);
        }
    }
}