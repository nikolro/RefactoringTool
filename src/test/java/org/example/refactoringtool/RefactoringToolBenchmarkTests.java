package org.example.refactoringtool;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RefactoringToolBenchmarkTests extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Add required classes from the standard library
        myFixture.addClass("package java.util; public interface List<T> { boolean add(T element); T get(int index); }");
        myFixture.addClass("package java.util; public class ArrayList<T> implements List<T> { public boolean add(T element) { return true; } public T get(int index) { return null; } }");
        myFixture.addClass("package java.util; public class Collections { public static <T> List<T> emptyList() { return null; } }");

        // Add other common utility classes if needed
        myFixture.addClass("package java.util; public class Arrays { public static <T> List<T> asList(T... a) { return null; } }");

        // Add mock implementations or any other classes your plugin interacts with
        myFixture.addClass("package com.example; public class MyCustomClass { public void doSomething() {} }");
    }

    @Test
    public void testBenchmark1() throws Exception {
        applyQuickFixesAndCompare("benchmark1.java");
    }

    @Test
    public void testBenchmark2() throws Exception {
        applyQuickFixesAndCompare("benchmark2.java");
    }

    @Test
    public void testBenchmark3() throws Exception {
        applyQuickFixesAndCompare("benchmark3.java");
    }

    @Test
    public void testBenchmark4() throws Exception {
        applyQuickFixesAndCompare("benchmark4.java");
    }

    @Test
    public void testBenchmark5() throws Exception {
        applyQuickFixesAndCompare("benchmark5.java");
    }

    @Test
    public void testBenchmark6() throws Exception {
        applyQuickFixesAndCompare("benchmark6.java");
    }

    @Test
    public void testBenchmark7() throws Exception {
        applyQuickFixesAndCompare("benchmark7.java");
    }

    @Test
    public void testBenchmark8() throws Exception {
        applyQuickFixesAndCompare("benchmark8.java");
    }

    @Test
    public void testBenchmark9() throws Exception {
        applyQuickFixesAndCompare("benchmark9.java");
    }

    public void applyQuickFixesAndCompare(@NotNull String benchmarkFileName) throws Exception {
        String filePath = getTestDataPath() + "/" + benchmarkFileName;
        String fileContent = loadFile(filePath);
        String programBeforeContent = extractContent(fileContent, "programBefore");
        String programAfterContent = extractContent(fileContent, "programAfter");

        VirtualFile virtualFile = myFixture.getTempDirFixture().createFile("TemporaryProgramBefore.java", programBeforeContent);
        myFixture.configureFromExistingVirtualFile(virtualFile);

        System.out.println("Content of TemporaryProgramBefore.java:");
        System.out.println(programBeforeContent);
        System.out.println("End of TemporaryProgramBefore.java content");

        myFixture.enableInspections(MyInspection.class);
        myFixture.doHighlighting();

        List<IntentionAction> intentions = myFixture.getAllQuickFixes();
        int appliedFixes = 0;
        for (IntentionAction intention : intentions) {
            if (intention.getText().equals("Apply wildcard refactoring")) {
                myFixture.launchAction(intention);
                appliedFixes++;
            }
        }

        String actualResult = myFixture.getFile().getText().trim();
        boolean matches = compareIgnoringWhitespace(programAfterContent, actualResult);
        String result = matches ? "PASS" : "FAIL";
        String resultString = String.format("%s: Applied %d fixes, Result: %s%n",
                benchmarkFileName, appliedFixes, result);
        System.out.print(resultString);

        assertTrue("The refactored code does not match the expected result for " + benchmarkFileName, matches);
    }

    private boolean compareIgnoringWhitespace(String expected, String actual) {
        // Split the strings into lines
        String[] expectedLines = expected.split("\n");
        String[] actualLines = actual.split("\n");

        // Join the lines and remove all whitespace
        String processedExpected = String.join("", expectedLines).replaceAll("\\s+", "");
        String processedActual = String.join("", actualLines).replaceAll("\\s+", "");

        // Compare the processed strings
        return processedExpected.equals(processedActual);
    }

    @NotNull
    private String loadFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filePath, e);
        }
    }

    private String extractContent(String fileContent, String marker) {
        String[] lines = fileContent.split("\n");
        StringBuilder content = new StringBuilder();
        boolean extracting = false;
        int braceCount = 0;

        for (String line : lines) {
            if (line.contains("class " + marker)) {
                extracting = true;
            }

            if (extracting) {
                content.append(line).append("\n");

                // Count braces to determine when to stop extraction
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                    }
                }

                if (braceCount == 0 && extracting) {
                    extracting = false;
                }
            }
        }

        // Convert to a string and split into lines
        String contentString = content.toString().trim();
        String[] contentLines = contentString.split("\n");

        // Remove the first and last lines
        if (contentLines.length > 2) {
            StringBuilder trimmedContent = new StringBuilder();
            for (int i = 1; i < contentLines.length - 1; i++) {
                trimmedContent.append(contentLines[i]).append("\n");
            }
            return trimmedContent.toString().trim();
        }

        // If less than 3 lines, return an empty string
        return "";
    }
}
