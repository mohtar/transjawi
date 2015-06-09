////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.util.*;

abstract class Token {

    abstract public int length();

    public static final class Match extends Token {
        
        int length;
        
        public Match(int length) {
            this.length = length;
        }

        public int length() {
            return length;
        }

        public String toString() {
            return "Match(" + length + ")";
        }
    }

    public static final class Word extends Token {
        
        public int length;
        
        public Word(int length) {
            this.length = length;
        }

        public int length() {
            return length;
        }

        public String toString() {
            return "Word(" + length + ")";
        }
    }

    public static final class Punc extends Token {
        
        public Punc() {
        }

        public int length() {
            return 1;
        }

        public String toString() {
            return "Punc()";
        }
    }
}
