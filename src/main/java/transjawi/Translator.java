////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//  Copyright (C) 2015 Mohd Tarmizi Mohd Affandi                              //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

package transjawi;

import java.util.*;
import java.io.*;

public class Translator {

    Trie<List<Entry>> trie;

    public Translator(Trie<List<Entry>> trie) {
        this.trie = trie;
    }

    public String processTokens(String source, List<Token> tokens,
            List<Token> output) {
        StringBuilder result = new StringBuilder();
        int i = 0; // Source offset
        int j = 0; // Token index
        while (j < tokens.size()) {
            Token token = tokens.get(j);
            if (token instanceof Token.Match) {
                List<Entry> segments = new ArrayList<Entry>();
                while (j < tokens.size()) {
                    token = tokens.get(j);
                    if (token instanceof Token.Match) {
                        Entry segment = null;
                        String text = source.substring(i, i + token.length()).toUpperCase(Locale.ENGLISH);
                        List<Entry> entries = trie.get(text);
                        List<Entry.Clue.Disambig> dataset = new ArrayList<Entry.Clue.Disambig>();
                        for (Entry entry : entries) {
                            Entry.Clue.Disambig disambig = entry.getDisambig();
                            if (disambig == null) {
                                segment = entry;
                                break;
                            } else {
                                dataset.add(disambig);
                            }
                        }
                        if (segment == null) {
                            List<String> context = getContext(source, tokens, i, j);
                            segment = entries.get(disambiguate(dataset, context));
                        }
                        segments.add(segment);
                        i += token.length();
                        j++;
                    } else {
                        break;
                    }
                }
                String[] s = joinSegments(segments.toArray(new Entry[segments.size()]));
                for (String segment : s) {
                    result.append(segment);
                    output.add(new Token.Match(segment.length()));
                }
            } else {
                result.append(source.substring(i, i + token.length()));
                output.add(token);
                i += token.length();
                j++;
            }
        }
        return result.toString();
    }

    static List<String> scanWords(String source) {
        List<String> words = new ArrayList<String>();
        int i = 0;
        while (i < source.length()) {
            int length = scanWord(source, i);
            if (length > 0) {
                words.add(source.substring(i, i + length));
                i += length;
            } else {
                i++;
            }
        }
        return words;
    }

    static List<String> getContext(String source, List<Token> tokens, int i, int j) {
        List<String> keywords = getContextL(source, tokens, i, j);
        keywords.addAll(getContextR(source, tokens, i, j));
        return keywords;
    }

    static List<String> getContextL(String source, List<Token> tokens, int i, int j) {
        List<String> keywords = new ArrayList<String>();
        j--;
        while (j >= 0 && keywords.size() < 5) {
            Token token = tokens.get(j);
            String text = source.substring(i - token.length(), i).toUpperCase(Locale.ENGLISH);
            if (token instanceof Token.Match) {
                List<String> words = scanWords(text);
                if (words.size() > (5 - keywords.size())) {
                    words.subList(0, 5 - keywords.size()).clear();
                }
                keywords.addAll(words);
            } else if (token instanceof Token.Word) {
                keywords.add(text);
            }
            i -= token.length();
            j--;
        }
        return keywords;
    }

    static List<String> getContextR(String source, List<Token> tokens, int i, int j) {
        List<String> keywords = new ArrayList<String>();
        i += tokens.get(j).length();
        j++;
        while (j < tokens.size() && keywords.size() < 5) {
            Token token = tokens.get(j);
            String text = source.substring(i, i + token.length()).toUpperCase(Locale.ENGLISH);
            if (token instanceof Token.Match) {
                List<String> words = scanWords(text);
                if (words.size() > (5 - keywords.size())) {
                    words.subList(words.size() - (5 - keywords.size()), words.size()).clear();
                }
                keywords.addAll(words);
            } else if (token instanceof Token.Word) {
                keywords.add(text);
            }
            i += token.length();
            j++;
        }
        return keywords;
    }

    public String translate(String source) {
        List<Token> tokens = scan(source);
        return processTokens(source, tokens, new ArrayList());
    }

    List<Token> scan(String source) {
        String uppercaseSource = source.toUpperCase(Locale.ENGLISH);
        List<Token> tokens = new ArrayList<Token>();
        int i = 0;
        while (i < source.length()) {
            List<Integer> segments = scanCompound(uppercaseSource, i);
            if (segments != null) {
                for (int segment : segments) {
                    tokens.add(new Token.Match(segment));
                    i += segment;
                }
                continue;
            }
            int word = scanWord(source, i);
            if (word > 0) {
                tokens.add(new Token.Word(word));
                i += word;
                continue;
            }
            tokens.add(new Token.Punc());
            i++;
        }
        return tokens;
    }

    List<Integer> scanCompound(String source, int offset) {
        List<Integer> prefixes = trie.walk(source, offset);
        for (int i = prefixes.size() - 1; i >= 0; i--) {
            int prefix = prefixes.get(i);
            List<Integer> segments = scanCompound(source, offset + prefix);
            if (segments == null && isWordBoundary(source, offset + prefix)) {
                segments = new ArrayList<Integer>();
            }
            if (segments != null) {
                segments.add(0, prefix);
                return segments;
            }
        }
        return null;
    }

    static int scanWord(String source, int offset) {
        int result = 0;
        while (!isWordBoundary(source, offset + result)) {
            result++;
        }
        return result;
    }

    static boolean isWordBoundary(String source, int offset) {
        return offset >= source.length() || !(Character.isAlphabetic(source.charAt(offset)) || Character.isDigit(source.charAt(offset)));
    }

    static int disambiguate(List<Entry.Clue.Disambig> dataset, List<String> context) {
        double[] score = new double[dataset.size()];
        for (int i = 0; i < dataset.size(); i++) {
            double total = 0;
            for (Map.Entry<String, Integer> entry : dataset.get(i).keywords.entrySet()) {
                total += entry.getValue();
            }
            for (String word : context) {
                if (dataset.get(i).keywords.containsKey(word)) {
                    score[i] += Math.log(((double)dataset.get(i).keywords.get(word) / total) + 1);
                }
            }
        }
        int max = 0;
        for (int i = 0; i < score.length; i++) {
            if (score[i] > score[max]) {
                max = i;
            }
        }
        return max;
    }

    static String[] joinSegments(Entry[] segments) {
        String[] result = new String[segments.length];
        result[0] = segments[0].translation;
        for (int i = 0; i < segments.length - 1; i++) {
            Entry x = segments[i];
            Entry y = segments[i + 1];
            result[i + 1] = "";
            if (x.isStandalone()) {
                if (x.original.endsWith("A") && !x.translation.endsWith("ا") && !x.translation.endsWith("ء")) {
                    if (x.translation.endsWith("ى")) {
                        result[i] = result[i].substring(0, result[i].length() - 1);
                    }
                    result[i] += "ا";
                } else if (x.original.endsWith("I") && !x.translation.endsWith("ي")) {
                    result[i] += "ي";
                } else if (x.original.endsWith("U") && !x.translation.endsWith("و")) {
                    result[i] += "و";
                }
                if (y.isStandalone()) {
                    char[] joiners = new char[] { // Sorted
                        'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'س', 'ش', 'ص', 'ض', 'ط',
                        'ظ', 'ع', 'غ', 'ف', 'ق', 'ل', 'م', 'ن', 'ه', 'ي', 'چ',
                        'ڠ', 'ڤ', 'ک', 'ڽ', 'ݢ'};
                    char[] nonjoiners = new char[] { // Sorted
                        'ء', 'أ', 'ؤ', 'ا', 'ة', 'د', 'ذ', 'ر', 'ز', 'و', 'ۏ'};
                    if (!y.original.startsWith("A") && y.translation.startsWith("ا")) {
                        if (Arrays.binarySearch(joiners, result[i].charAt(result[i].length() - 1)) >= 0) {
                            result[i + 1] += "ئ";
                        } else if (Arrays.binarySearch(nonjoiners, result[i].charAt(result[i].length() - 1)) >= 0) {
                            result[i + 1] += "ء";
                        }
                    }
                }
            }
            if (y.isStandalone()) {
                if (x.isStandalone() || (x.isPrefix() && (x.original.equals("MENG") || x.original.equals("PENG")))) {
                    if (y.original.startsWith("I") && y.translation.startsWith("ا")) {
                        if (!y.translation.startsWith("اي")) {
                            result[i + 1] += "ي";
                        }
                        result[i + 1] += y.translation.substring(1);
                    } else if ((y.original.startsWith("O") || y.original.startsWith("U")) && y.translation.startsWith("ا")) {
                        if (!y.translation.startsWith("او")) {
                            result[i + i] += "و";
                        }
                        result[i + 1] += y.translation.substring(1);
                    } else if (y.original.startsWith("E") && y.translation.startsWith("ا")) {
                        result[i + 1] += y.translation.substring(1);
                    } else {
                        result[i + 1] += y.translation;
                    }
                } else if ((x.original.equals("DI") || x.original.equals("KE") || x.original.equals("SE")) && y.translation.startsWith("ا")) {
                    result[i + 1] += "أ";
                    result[i + 1] += y.translation.substring(1);
                } else {
                    result[i + 1] += y.translation;
                }
            } else {
                result[i + 1] += y.translation;
            }
        }
        return result;
    }
}
