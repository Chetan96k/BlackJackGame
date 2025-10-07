package com.kodnest;

import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlackJack {
    // Card Class
    private class Card {
        String value;
        String type;

        public Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) {
                if (value.equals("A")) { // A J Q K
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value); // 2 - 10
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "/cards/" + toString() + ".png";
        }
    }

    // Card combinations
    final String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    final String[] types = {"C", "S", "H", "D"};
    List<Card> deck; // Stores the cards

    // Dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    // Player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    // Window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    int cardWidth = 110;
    int cardHeight = 154;

    JFrame frame = new JFrame("♣ BLACKJACK ♠");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                // Background felt
                setBackground(new Color(13, 17, 13)); // Deep green felt

                // Draw golden border
                g.setColor(new Color(0xFF, 0xD7, 0x00));
                g.drawRoundRect(10, 10, boardWidth - 40, boardHeight - 80, 30, 30);

                // Title text
                g.setFont(new Font("Georgia", Font.BOLD, 30));
                g.setColor(new Color(0xFF, 0xD7, 0x00));
                g.drawString("BLACKJACK", 210, 50);

                // Load and draw the hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("/cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 20, 80, cardWidth, cardHeight, null);

                // Draw dealers hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon((getClass().getResource(card.getImagePath()))).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i, 80, cardWidth, cardHeight, null);
                }

                // Draw players hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon((getClass().getResource(card.getImagePath()))).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5) * i, 350, cardWidth, cardHeight, null);
                }

                // Draw player/dealer labels
                g.setFont(new Font("SansSerif", Font.BOLD, 18));
                g.setColor(Color.WHITE);
                g.drawString("Dealer", 20, 70);
                g.drawString("Player", 20, 340);

                // Display result
                if (!stayButton.isEnabled()) {
                    dealerSum = reduceAce(dealerSum, dealerAceCount);
                    playerSum = reduceAce(playerSum, playerAceCount);

                    String message = "";
                    if (playerSum > 21) {
                        message = "You Lost!";
                        g.setColor(Color.RED);
                    } else if (dealerSum > 21) {
                        message = "You Won!";
                        g.setColor(new Color(0xFF, 0xD7, 0x00));
                    } else if (playerSum == dealerSum) {
                        message = "Tie!";
                        g.setColor(Color.LIGHT_GRAY);
                    } else if (playerSum > dealerSum) {
                        message = "You Won!";
                        g.setColor(new Color(0xFF, 0xD7, 0x00));
                    } else {
                        message = "Dealer Wins!";
                        g.setColor(Color.RED);
                    }

                    g.setFont(new Font("Arial", Font.BOLD, 30));
                    g.drawString(message, 220, 300);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");

    // Start of the game
    BlackJack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        frame.add(gamePanel);

        // Button Panel Design
        buttonPanel.setBackground(new Color(0x3E, 0x27, 0x23)); // Dark wood tone
        buttonPanel.setBorder(BorderFactory.createLineBorder(new Color(0xFF, 0xD7, 0x00), 2));

        styleButton(hitButton, new Color(0x22, 0x8B, 0x22)); // Deep green
        styleButton(stayButton, new Color(0xB2, 0x22, 0x22)); // Rich red

        buttonPanel.add(hitButton);
        buttonPanel.add(stayButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size() - 1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reduceAce(playerSum, playerAceCount) > 21) {
                    hitButton.setEnabled(false);
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }

                gamePanel.repaint();
            }
        });

        gamePanel.repaint();
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(BorderFactory.createLineBorder(new Color(0xFF, 0xD7, 0x00), 2));
    }

    public void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size() - 1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        playerHand = new ArrayList<>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }
    }

    public void buildDeck() {
        deck = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < types.length; j++) {
                deck.add(new Card(values[i], types[j]));
            }
        }
    }

    Random random = new Random();

    public void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public int reduceAce(int sum, int aceCount) {
        while (sum > 21 && aceCount > 0) {
            sum -= 10;
            aceCount--;
        }
        return sum;
    }
}
