package org.hazelv.chime.trans;

import javax.sound.midi.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
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
    public static HashMap<String, Integer> labels;

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
        if (outputFile == null) {
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

            String[] lines = inputString.split("\n");
            for (String line : lines) {
                String[] parts = line.split(" ");
                System.out.println(Arrays.toString(parts));
                for (String part : parts) {
                    switch (part) {
                        case "bgn": // start header
                            addChord(StartChord.class);
                            addChord(Start2Chord.class);
                            break;
                        case "ldi": // Load Immediate or hold
                            addChord(HoldChord.class);
                            break;
                        case "add": // math
                            addChord(AddChord.class);
                            break;
                        case "sub":
                            addChord(SubtractChord.class);
                            break;
                        case "mul":
                            addChord(MultiplyChord.class);
                            break;
                        case "div":
                            addChord(DivideChord.class);
                            break;
                        case "prt": // IO
                            addChord(PrintChord.class);
                            break;
                        case "pln":
                            addChord(PrintChord.class);
                            break;
                        case "pch":
                            addChord(PrintCharChord.class);
                            break;
                        case "ipt":
                            addChord(InputChord.class);
                            break;
                        case "cva": //current value
                            addChord(CurrentValChord.class);
                            break;
                        case "evl": //Control Flow
                            addChord(EvalChord.class);
                            break;
                        case "jmp":
                            addChord(JumpChord.class);
                            break;
                        case "jeq":
                            addChord(JumpIfChord.class);
                            break;
                        case "psh":
                            addChord(PushChord.class);
                            break;
                        case "pop":
                            addChord(PopChord.class);
                            break;
                        default:
                            if (Pattern.compile("^\\d+$").matcher(part).find()) { // number
                                addNote(NoteName.values()[Integer.parseInt(part)], false);
                            } else if (Pattern.compile("^_.*:$").matcher(part).find()) {
                                labels.put(part, programCounter);
                            } else if (Pattern.compile("^_.*").matcher(part).find()) {
                                addNote(NoteName.values()[labels.get(part)], false);
                            } else {
                                throw new IllegalArgumentException("Invalid.");
                            }
                    }
                }
            }

            MidiSystem.write(sequence, 1, outputFile);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addChord(Class<?> chordType) throws InvalidMidiDataException {
        List<NoteName> notes = getKeysByValue(chordMap, chordType);
        for (NoteName note : notes) {
            addNote(note, true);
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

    public static void addNote(NoteName note, boolean inChord) throws InvalidMidiDataException {
        MidiEvent noteOn = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, note.ordinal(), 80), programCounter);
        track.add(noteOn);
        MidiEvent noteOff = new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, note.ordinal(), 0), programCounter + 1);
        track.add(noteOff);
        if (!inChord) {
            programCounter++;
        }
    }
}
