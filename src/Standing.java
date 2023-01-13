public class Standing implements Comparable<Standing> {
    private int year;
    private int score;
    private int position;

    public void Standing(int _year, int _score, int _position) {
        this.year = _year;
        this.score = _score;
        this.position = _position;
    }

    public int getYear() {
        return year;
    }

    public int getScore() {
        return score;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int compareTo(Standing s) {
        return(year - s.year);
    }
}
