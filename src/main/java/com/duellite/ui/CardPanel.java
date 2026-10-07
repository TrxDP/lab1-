package com.duellite.ui;

import com.duellite.model.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;



public class CardPanel extends JPanel {

    private static final int IMG_W = 120;
    private static final int IMG_H = 175;

    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel nameLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel statsLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton attackButton = new JButton("Atacar");
    private final JButton defendButton = new JButton("Defender");


    private BufferedImage currentImage;

    public CardPanel(boolean withButtons) {
        setLayout(new BorderLayout(4, 4));
        setPreferredSize(new Dimension(170, withButtons ? 330 : 300));
        setMinimumSize(new Dimension(140, withButtons ? 150 : 120));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55), 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setBackground(new Color(35, 30, 55));

        imageLabel.setPreferredSize(new Dimension(IMG_W, IMG_H));
        imageLabel.setMinimumSize(new Dimension(30, 30)); // la imagen es lo unico que se encoge
        imageLabel.setForeground(Color.WHITE);
        imageLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateIcon();
            }
        });

        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        statsLabel.setForeground(new Color(255, 230, 150));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(nameLabel);
        info.add(statsLabel);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.setOpaque(false);
        south.add(info, BorderLayout.CENTER);
        if (withButtons) {
            JPanel buttons = new JPanel(new GridLayout(1, 2, 4, 0));
            buttons.setOpaque(false);
            attackButton.setMargin(new Insets(2, 4, 2, 4));
            defendButton.setMargin(new Insets(2, 4, 2, 4));
            buttons.add(attackButton);
            buttons.add(defendButton);
            south.add(buttons, BorderLayout.SOUTH);
        }


        add(imageLabel, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        showEmpty();
    }

    public JButton getAttackButton() { return attackButton; }
    public JButton getDefendButton() { return defendButton; }


    public void showEmpty() {
        currentImage = null;
        imageLabel.setIcon(null);
        imageLabel.setText("Sin carta");
        nameLabel.setText(" ");
        statsLabel.setText(" ");
        setCardButtonsEnabled(false);
    }


    public void showCard(Card card, BufferedImage image) {
        setImage(image);
        nameLabel.setText(shorten(card.getName()));
        nameLabel.setToolTipText(card.getName());
        statsLabel.setText("ATK " + card.getAtk() + " / DEF " + card.getDef());
    }


    public void showHidden() {
        currentImage = null;
        imageLabel.setIcon(null);
        imageLabel.setText("?");
        imageLabel.setFont(imageLabel.getFont().deriveFont(Font.BOLD, 56f));
        nameLabel.setText("Carta oculta");
        nameLabel.setToolTipText(null);
        statsLabel.setText(" ");
    }


    public void reveal(Card card, BufferedImage image, boolean attack) {
        imageLabel.setFont(new JLabel().getFont());
        showCard(card, image);
        statsLabel.setText(attack ? "ATAQUE (" + card.getAtk() + ")" : "DEFENSA (" + card.getDef() + ")");
        setBackground(new Color(70, 50, 50));
    }


    public void setUsed(boolean used) {
        if (!used && getBackground().getRed() == 70) {
            setBackground(new Color(35, 30, 55));
        }
        if (used) {
            statsLabel.setText(statsLabel.getText().startsWith("USADA") ? statsLabel.getText()
                    : "USADA - " + statsLabel.getText());
            setBackground(new Color(60, 60, 60));
        }
    }

    public void setCardButtonsEnabled(boolean enabled) {
        attackButton.setEnabled(enabled);
        defendButton.setEnabled(enabled);
    }

    private void setImage(BufferedImage image) {
        currentImage = image;
        if (image == null) {
            imageLabel.setIcon(null);
            imageLabel.setText("Sin imagen");
        } else {
            imageLabel.setText("");
            updateIcon();
        }
    }


    private void updateIcon() {
        if (currentImage == null) return;
        int w = imageLabel.getWidth();
        int h = imageLabel.getHeight();
        if (w <= 0 || h <= 0) {
            w = IMG_W;
            h = IMG_H;
        }
        double scale = Math.min((double) w / currentImage.getWidth(), (double) h / currentImage.getHeight());
        int tw = Math.max(1, (int) (currentImage.getWidth() * scale));
        int th = Math.max(1, (int) (currentImage.getHeight() * scale));
        imageLabel.setIcon(new ImageIcon(currentImage.getScaledInstance(tw, th, Image.SCALE_SMOOTH)));
    }

    private String shorten(String name) {
        return name.length() > 22 ? name.substring(0, 21) + "…" : name;
    }
}