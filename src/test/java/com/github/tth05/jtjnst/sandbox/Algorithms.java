package com.github.tth05.jtjnst.sandbox;

import com.github.tth05.jtjnst.TestJavaCompilerHelper;
import com.github.tth05.jtjnst.TempDirTest;
import org.junit.jupiter.api.Test;

/**
 * Some example programs
 */
public class Algorithms extends TempDirTest {

    @Test
    public void testRunQuicksort() {
        // language=Java
        String input = "import java.util.Arrays;class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int[] array = new int[] {6,5,4,78,23,1,-5,-3,3567367};\n" +
                       "        quickSort(array, 0, array.length - 1);\n" +
                       "        System.out.println(Arrays.toString(array));\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void quickSort(int[] arr, int begin, int end) {\n" +
                       "        if (begin < end) {\n" +
                       "            int partitionIndex = partition(arr, begin, end);\n" +
                       "\n" +
                       "            quickSort(arr, begin, partitionIndex-1);\n" +
                       "            quickSort(arr, partitionIndex+1, end);\n" +
                       "        }\n" +
                       "    }\n" +
                       "\n" +
                       "    private static int partition(int[] arr, int begin, int end) {\n" +
                       "        int pivot = arr[end];\n" +
                       "        int i = (begin-1);\n" +
                       "\n" +
                       "        for (int j = begin; j < end; j++) {\n" +
                       "            if (arr[j] <= pivot) {\n" +
                       "                i++;\n" +
                       "\n" +
                       "                int swapTemp = arr[i];\n" +
                       "                arr[i] = arr[j];\n" +
                       "                arr[j] = swapTemp;\n" +
                       "            }\n" +
                       "        }\n" +
                       "\n" +
                       "        int swapTemp = arr[i+1];\n" +
                       "        arr[i+1] = arr[end];\n" +
                       "        arr[end] = swapTemp;\n" +
                       "\n" +
                       "        return i+1;\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "[-5, -3, 1, 4, 5, 6, 23, 78, 3567367]");
    }
}
