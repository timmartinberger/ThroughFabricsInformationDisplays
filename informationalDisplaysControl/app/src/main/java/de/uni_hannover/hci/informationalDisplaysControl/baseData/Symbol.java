package de.uni_hannover.hci.informationalDisplaysControl.baseData;

public enum Symbol {
    MONEY,
    HEART,
    HOUSE,
    CIRCLE,
    SQUARE,
    QUESTION_MARK,
    BELL,
    MUSIC,
    TIME,
    KEY,
    DUCK,
    GLASSES,
    CAT,
    CHERRY,
    LOCK;

    public String[] getDrawableArray() {
        return codeFromList() ;
    }

    private String[] lockCode = {"", ""};
    private String[] codeFromList() {
        switch(this) {
            case MONEY:
                break;
            case HEART:
                break;
            case HOUSE:
                break;
            case CIRCLE:
                break;
            case SQUARE:
                break;
            case QUESTION_MARK:
                break;
            case BELL:
                break;
            case MUSIC:
                break;
            case TIME:
                break;
            case KEY:
                break;
            case DUCK:
                break;
            case GLASSES:
                break;
            case CAT:
                break;
            case CHERRY:
                break;
            case LOCK:
                return lockCode;
        }
        return null;
    }
}


