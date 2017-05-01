package parser;

public class PTBlock {
    static class ActionType {
        static final int Error = 0;
        static final int Shift = 1;
        static final int Goto = 2;
        static final int PushGoto = 3;
        static final int Reduce = 4;
        static final int Accept = 5;
    }

    private String _sem;

    private int _index;

    private int _act;

    /**
     * @return the semantic function
     */
    public String getSem() {
        return _sem;
    }

    void setSem(String _sem) {
        this._sem = _sem;
    }

    // getIndex is used for parsing in parser class, or error reporting
    public int getIndex() {
        return _index;
    }

    void setIndex(int _index) {
        this._index = _index;
    }

    /**
     * @return the action type
     */
    public int getAct() {
        return _act;
    }

    void setAct(int _act) {
        this._act = _act;
    }

}