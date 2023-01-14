public class Standing implements Comparable<Standing> {
    public int year;
    public float score;
    public int position;

    public Standing(int year, float score, int position) {
        this.year = year;
        this.score = score;
        this.position = position;
    }

    @Override
    public int compareTo(Standing s) {
        return(year - s.year);
    }

    @Override
    public String toString() {
        return "Standing{" +
                "year=" + year +
                ", score=" + score +
                ", position=" + position +
                '}';
    }
}
