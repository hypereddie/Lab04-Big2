/**
 * Class: COMP 2071
 * Assignment: Lab 4
 * Due Date: 3/17/16
 * Group #: 21
 * Group Members:   Andrew Corp
 *                  Eddie Penta
 *                  Jacob Ollila
 */

import java.util.Arrays;

public class Game {

    private Player[] players;
    private Deck theDeck;
    private List<Card> currentMiddle;


    public Game() {
        setup();
    }

    private void setup() {
        players = new Player[4];
        currentMiddle = new List<>();
        theDeck = new Deck();
        theDeck.shuffle();

        //deal to all players
        for(int p = 0; p < 4; p++){
            if (p == 0)
                players[p] = new HumanPlayer();
            else
                players[p] = new ComputerPlayer();

            players[p].setPosition(p);

            for(int i = 0; i < 13; i++){
                players[p].getHand().addCardToHand(theDeck.dealCard());
            }
        }

        //print hands of players
        for(int i = 0; i < 4; i++) {
            Player p = players[i];
            System.out.println("Player " + (i+1) + " card's: ");
            p.getHand().printHand();
        }
        System.out.println();
    }

    public void reset() {
        setup();
    }

    public void start() {
        int turn = 0;
        List<Player> passed = new List<>();
        while (true) { //Continue until someone wins
            Player currentPlayer = players[turn];

            if (passed.contains(currentPlayer)) {
                turn++;
                turn = turn % 4;
                continue;
            }

            System.out.println("============== Player " + (turn + 1) + " ==============");
            do {
                List<Card> cards = currentPlayer.makeMove(this);

                if (cards == null) {
                    System.out.println("Passed");
                    passed.add(currentPlayer);
                    break;
                }

                Result request = requestMove(cards);
                if (request.didPass()) {
                    System.out.println("Played: ");
                    for (Card c : cards) {
                        System.out.println("    " + c);
                    }
                    currentPlayer.getHand().removeCards(cards);
                    break;
                } else {
                    System.out.println(request.getReason());
                }
            } while (true);

            if (currentPlayer.getHand().getNumOfCards() == 0) {
                setScoring(players);
                System.out.println("Player " + (turn + 1) + " wins!");
                System.out.println();
                System.out.println("Scores:");
                for (Player p : players) {
                    System.out.println("    Player " + (p.getPosition() + 1) + ": " + p.getScore());
                }
                break;
            }

            turn++;
            turn = turn % 4;
            System.out.println("============================");

            if (passed.getLength() == 3) {
                System.out.println();
                System.out.println("New round!");

                List<Integer> temp = new List<>(Arrays.asList(0, 1, 2, 3));
                for (Player p : passed) {
                    temp.remove(new Integer(p.getPosition()));
                }
                turn = temp.getEntry(0); //The last number remaining is the current turn

                currentMiddle.clear();
                passed.clear();
            }
            System.out.println();
        }
    }

    public Result requestMove(List<Card> toPlay) {
        toPlay.sort();

        Result result = isValidMove(toPlay);
        if (!result.didPass())
            return result;

        currentMiddle = toPlay;
        return result;
    }

    public Result isValidMove(List<Card> toPlay) {
        if (!isAllSame(toPlay) && !isStream(toPlay))
            return Result.fail("Cannot play this type of pattern!");

        if (currentMiddle.getLength() == 0) {
            if (toPlay.getCurrentSize() == 1 || isAllSame(toPlay) || isStream(toPlay)) {
                return Result.success();
            } else {
                return Result.fail("Cannot open with this type of pattern!");
            }
        } else {
            if (currentMiddle.getLength() != toPlay.getCurrentSize()) {
                if (currentMiddle.getLength() == 1 && currentMiddle.getEntry(0).equals(Card.BIG2)) {
                    if (toPlay.getCurrentSize() == 4 && isAllSame(toPlay)) //4 of a kind beats big 2
                        return Result.success();
                }
                return Result.fail("Incorrect number of cards, you need to place " + currentMiddle.getLength() + " card" + (currentMiddle.getLength() == 1 ? "" : "s"));
            }
            else if (isAllSame(currentMiddle) && isStream(toPlay))
                return Result.fail("You need to play pairs, not a stream!");
            else if (isStream(currentMiddle) && isAllSame(toPlay))
                return Result.fail("You need to play a stream, not pairs!");
            else {
                //Cards are always sorted from lowest to highest
                Card highestMiddle = currentMiddle.getEntry(currentMiddle.getLength() - 1);
                Card highestPlay = toPlay.getEntry(toPlay.getCurrentSize() - 1);

                if (highestMiddle.isHigher(highestPlay))
                    return Result.fail("The card in the middle is higher!");
            }
        }

        return Result.success();
    }

    public List<Card> getCurrentMiddle() {
        return currentMiddle;
    }

    private boolean isAllSame(Card[] cards) {
        return isAllSame(new List<Card>(cards));
    }

    private boolean isStream(Card[] cards) {
        return isStream(new List<Card>(cards));
    }

    private boolean isAllSame(List<Card> cards) {
        int lastValue = cards.getEntry(0).getBig2Value();
        for (int i = 1; i < cards.getCurrentSize(); i++) {
            if (cards.getEntry(i).getBig2Value() != lastValue)
                return false;
        }
        return true;
    }

    private boolean isStream(List<Card> cards) {
        if (cards.getCurrentSize() < 3)
            return false;

        int lastValue = cards.getEntry(0).getBig2Value();
        for (int i = 1; i < cards.getCurrentSize(); i++) {
            if (cards.getEntry(i).getBig2Value() != lastValue + 1)
                return false;
            lastValue = cards.getEntry(i).getBig2Value();
        }

        return true;
    }


    //going by this scoring https://en.wikipedia.org/wiki/Big_Two#Scoring
    private void setScoring(Player[] players){
        int negativeScore = 0;
        for(Player p : players){
            int countCards = p.getHand().getNumOfCards();
            if(countCards == 13){
                p.setScore(13*(-3));
                negativeScore += (13*3);
            } else if(countCards >= 10){
                p.setScore(countCards * (-2));
                negativeScore += (countCards*2);
            } else if (countCards > 0){
                p.setScore(countCards * (-1));
                negativeScore += countCards;
            } else {
                p.setScore(negativeScore);
            }
        }
    }

}