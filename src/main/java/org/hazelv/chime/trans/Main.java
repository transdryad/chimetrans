package org.hazelv.chime.trans;

import javax.sound.midi.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.hazelv.chime.lang.NoteName;
import org.hazelv.chime.lang.chords.*;

import static org.hazelv.chime.lang.Main.chordMap;

public class Main {
    public static File sourceFile;
    public static File outputFile;
    public static Sequence sequence;
    public static Track track;
    private static int programCounter = 0;
    public static String inputString;
    public static Scanner scanner;
    private static final Compiler compiler = new Compiler();

    public static void main(String[] args) throws IllegalArgumentException {
        if (!(args.length > 0)) {
            throw new IllegalArgumentException("A filename must be provided.");
        } else {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-o":
                        outputFile = new File(args[++i]);
                        i++;
                        break;
                    default:
                        if (sourceFile == null) {
                            sourceFile = new File(args[i]);
                        } else {
                            throw new IllegalArgumentException("Bad usage.");
                        }
                }
            }
        }
        if (!(outputFile == null)) {
            outputFile = new File("o.mid");
        }
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist.");
        }
        try {
            chordMap = new HashMap<>();
            org.hazelv.chime.lang.Main.registerDefaultChords();
            sequence = new Sequence(Sequence.PPQ, 24);
            track = sequence.createTrack();

            byte[] bytes = Files.readAllBytes(Paths.get(sourceFile.getPath()));
            inputString = new String(bytes, Charset.defaultCharset());
            // Scan
            scanner = new Scanner(inputString);
            List<Token> tokens = scanner.scanTokens();
            //Parse into AST
            Parser parser = new Parser(tokens);
            Expr expression = parser.parse();
            System.out.println(new AstPrinter().print(expression));

            // Start header
            addChord(StartChord.class);
            addChord(Start2Chord.class);
            //Compile & add
            compiler.compile(expression);
            //do all the languagey things
            //MidiSystem.write(sequence, 1, outputFile);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void addChord(Class<?> chordType) throws InvalidMidiDataException {
        List<NoteName> notes = getKeysByValue(chordMap, chordType);
        for (NoteName note : notes) {
            MidiEvent noteOn = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, note.ordinal(), 80), programCounter);
            track.add(noteOn);
            MidiEvent noteOff = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, note.ordinal(), 0), programCounter + 1);
            track.add(noteOff);
        }
        programCounter++;
    }

    public static <T, E> T getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()).iterator().next();
    }
}
