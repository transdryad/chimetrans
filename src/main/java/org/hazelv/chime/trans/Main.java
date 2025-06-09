// Copyright 2025 Hazel Viswanath <viswanath.hazel@gmail.com>.

// This file is part of Chime-Trans.

// Chime-Trans is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
// License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
// any later version.

// Chime-Trans is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
// the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See LICENSE in the project root for more details.

// You should have received a copy of the GNU General Public License along with Chime-Trans. If not, see <https://www.gnu.org/licenses/>.
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
    public static HashMap<String, Integer> labels =  new HashMap<>();
    public static boolean now = false;

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
                    case "--now":
                        now = true;
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
            throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " does not exist.");
        }
        try {
            chordMap = new HashMap<>();
            org.hazelv.chime.lang.Main.registerDefaultChords();
            sequence = new Sequence(Sequence.PPQ, 24);
            track = sequence.createTrack();

            byte[] bytes = Files.readAllBytes(Paths.get(sourceFile.getPath()));
            inputString = new String(bytes, Charset.defaultCharset());

            String[] lines = inputString.split("\n");
            int labelCounter = 1;
            for (String line : lines) {
                boolean comment = false;
                String[] parts = line.split(" ");
                for (String part : parts) {
                    if (comment) break;
                    if (!(part.equals(" ") || part.isEmpty() || part.equals("\n") ||  part.equals("\r\n") ||  part.equals("\t"))) {
                        if (Pattern.compile("^_.*:$").matcher(part).find()) {
                            labels.put(part.substring(0, part.length() - 1), labelCounter);
                        }
                        if (part.equals("end")) labelCounter++;
                        if (Pattern.compile("^;.*").matcher(part).find()) {
                            comment = true;
                        } else {
                            labelCounter++;
                        }
                    }
                }
            }
            for (int i = 0;  i < lines.length; i++) {
                String line = lines[i];
                boolean comment = false;
                String[] parts = line.split(" ");
                //System.out.println(Arrays.toString(parts));
                for (String part : parts) {
                    if (comment) break;
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
                            addChord(PrintLnChord.class);
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
                        case "jid": // Functions
                            addNote(NoteName.values()[programCounter + 5], false);
                            //System.out.println(programCounter + 5);
                            break;
                        case "end":
                            addChord(JumpChord.class);
                            addNote(NoteName.values()[labelCounter - 1], false);
                            break;
                        default:
                            if (Pattern.compile("^\\d+$").matcher(part).find()) { // number
                                addNote(NoteName.values()[Integer.parseInt(part)], false);
                            } else if (Pattern.compile("^_.*[^:]$").matcher(part).find()) {
                                addNote(NoteName.values()[labels.get(part)], false);
                            } else if (Pattern.compile("^;.*").matcher(part).find()) {
                                comment = true;
                            } else if (!(part.equals(" ") || part.isEmpty() || part.equals("\n") ||  part.equals("\r\n") ||  part.equals("\t")) && !(Pattern.compile("^_.*:$").matcher(part).find())) {
                                throw new IllegalArgumentException(String.format("Syntax error on line %s, with part %s", i + 1, part));
                            }
                    }
                }
            }

            addChord(PrintLnChord.class);

            MidiSystem.write(sequence, 1, outputFile);
            if (now) {
                org.hazelv.chime.lang.Main.main(new String[]{outputFile.getPath()});
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass() + ": " + e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
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
