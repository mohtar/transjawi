////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.io.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

class Lexicon {

    public static Trie<List<Entry>> load() throws IOException, ParseError {
        ANTLRInputStream input = new ANTLRInputStream(new InputStreamReader(
            Main.class.getResourceAsStream("/lexicon.txt"), "UTF-8"));
        LexiconLexer lexer = new LexiconLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LexiconParser parser = new LexiconParser(tokens);
        LexiconParser.ListContext list = parser.expr().list();
        Entry[] entries = new Entry[list.item().size()];
        int i = 0;
        for (LexiconParser.ItemContext item : list.item()) {
            LexiconParser.StructureContext struct = item.expr().structure();
            String original = withoutQuotes(struct.expr(0).STRING().getText());
            String translation = withoutQuotes(
                struct.expr(1).STRING().getText());
            Entry.Clue[] clues =
                new Entry.Clue[struct.expr(2).list().item().size()];
            int j = 0;
            for (LexiconParser.ItemContext item2 : struct.expr(2).list().item())
                    {
                Entry.Clue clue = null;
                String constructor =
                    item2.expr().structure().IDENTIFIER().getText();
                if (constructor.equals("Prefix")) {
                    clue = new Entry.Clue.Prefix();
                } else if (constructor.equals("Suffix")) {
                    clue = new Entry.Clue.Suffix();
                } else if (constructor.equals("Disambig")) {
                    List<LexiconParser.ExprContext> arguments =
                        item2.expr().structure().expr();
                    Map<String, Integer> keywords =
                        new HashMap<String, Integer>();
                    for (LexiconParser.ItemContext item3 :
                            arguments.get(0).list().item()) {
                        String key = withoutQuotes(item3.STRING().getText());
                        int value =
                            Integer.parseInt(item3.expr().NUMBER().getText());
                        keywords.put(key, value);
                    }
                    clue = new Entry.Clue.Disambig(keywords);
                } else if (constructor.equals("Punc")) {
                    clue = new Entry.Clue.Punc();
                } else {
                    throw new ParseError("Unknown clue.");
                }
                clues[j] = clue;
                j++;
            }
            entries[i] = new Entry(original, translation, clues);
            i++;
        }

        // Put into map.
        Map<String, List<Entry>> map = new HashMap<String, List<Entry>>();
        for (Entry entry : entries) {
            List<Entry> value = map.get(entry.original);
            if (value == null) {
                value = new ArrayList<Entry>();
                map.put(entry.original, value);
            }
            value.add(entry);
        }

        // Build the trie.
        Trie<List<Entry>> trie = new Trie<List<Entry>>();
        for (Map.Entry<String, List<Entry>> entry : map.entrySet()) {
            trie.set(entry.getKey(), entry.getValue());
        }
        
        // Done.
        return trie;
    }

    static String withoutQuotes(String token) {
        return token.substring(1, token.length() - 1);
    }

    static public class ParseError extends Exception {
        
        ParseError(String message) {
            super(message);
        }
    }
}
