////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.util.*;

class Entry {
    public String original;
    public String translation;
    public Clue[] clues;

    public Entry(String original, String translation, Clue[] clues) {
        this.original = original;
        this.translation = translation;
        this.clues = clues;
    }

    public boolean isPrefix() {
        for (Clue clue : clues) {
            if (clue instanceof Clue.Prefix) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuffix() {
        for (Clue clue : clues) {
            if (clue instanceof Clue.Suffix) {
                return true;
            }
        }
        return false;
    }

    public boolean isStandalone() {
        return !(isPrefix() || isSuffix());
    }

    public Clue.Disambig getDisambig() {
        for (Clue clue : clues) {
            if (clue instanceof Clue.Disambig) {
                return (Clue.Disambig)clue;
            }
        }
        return null;
    }

    public static abstract class Clue {
        
        public static final class Prefix extends Clue {
        
            public Prefix() {

            }
        }

        public static final class Suffix extends Clue {
        
            public Suffix() {

            }
        }

        public static final class Punc extends Clue {
        
            public Punc() {

            }
        }
        
        public static final class Disambig extends Clue {
            
            public Map<String, Integer> keywords;
            
            public Disambig(Map<String, Integer> keywords) {
                this.keywords = keywords;
            }
        }
    }
}
