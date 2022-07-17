package de.uni_hannover.hci.informationalDisplaysControl.baseData;

public enum Symbol {
    APPLE,
    KEY,
    TREE,
    MOON,
    CLOCK,
    CACTUS,
    EARTH,
    YING_YANG,
    LOCK,
    LIGHTNING,
    HEART,
    BELL,
    CHERRIES,
    MUSIC,
    QUESTION_MARK,
    STAR,
    BEER,
    GHOST,
    FLOWER,
    MCDONALDS,
    TROPHY,
    SWORD,
    DUCK,
    CROWN,
    BASEBALL,
    BIRD,
    DIAMOND,
    GARBAGE;
    // 0: apple, 1: key, 2: tree, 3: moon, 4: clock, 5: cactus,
    // 6: earth, 7: yingyang, 8: lock, 9: lightning, 10: heart,
    // 11: bell, 12: cheries, 13: music, 14: question mark,
    // 15: star, 16: beer, 17: ghost, 18: flower, 19: mecces,
    // 20: trophy, 21: sword, 22: duck, 23: crown, 24: baseball,
    // 25: bird, 26: diamond, 27: garbage

    public int getCode() {
        switch(this) {
            case APPLE:
                return 0;
            case KEY:
                return 1;
            case TREE:
                return 2;
            case MOON:
                return 3;
            case CLOCK:
                return 4;
            case CACTUS:
                return 5;
            case EARTH:
                return 6;
            case YING_YANG:
                return 7;
            case LOCK:
                return 8;
            case LIGHTNING:
                return 9;
            case HEART:
                return 10;
            case BELL:
                return 11;
            case CHERRIES:
                return 12;
            case MUSIC:
                return 13;
            case QUESTION_MARK:
                return 14;
            case STAR:
                return 15;
            case BEER:
                return 16;
            case GHOST:
                return 17;
            case FLOWER:
                return 18;
            case MCDONALDS:
                return 19;
            case TROPHY:
                return 20;
            case SWORD:
                return 21;
            case DUCK:
                return 22;
            case CROWN:
                return 23;
            case BASEBALL:
                return 24;
            case BIRD:
                return 25;
            case DIAMOND:
                return 26;
            case GARBAGE:
                return 27;
        }
        return -1;
    }
}


