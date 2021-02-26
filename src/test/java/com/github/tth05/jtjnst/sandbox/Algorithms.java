package com.github.tth05.jtjnst.sandbox;

import com.github.tth05.jtjnst.JavaCompilerHelper;
import com.github.tth05.jtjnst.TempDirTest;
import org.junit.jupiter.api.Test;

/**
 * Some example programs
 */
public class Algorithms extends TempDirTest {

    @Test
    public void testRunQuicksort() {
        // language=Java
        String input = """
                import java.util.Arrays;class Test {
                    public static void main(String[] args) {
                        int[] array = new int[] {6,5,4,78,23,1,-5,-3,3567367};
                        quickSort(array, 0, array.length - 1);
                        System.out.println(Arrays.toString(array));
                    }
                
                    public static void quickSort(int[] arr, int begin, int end) {
                        if (begin < end) {
                            int partitionIndex = partition(arr, begin, end);
                    
                            quickSort(arr, begin, partitionIndex-1);
                            quickSort(arr, partitionIndex+1, end);
                        }
                    }
                
                    private static int partition(int[] arr, int begin, int end) {
                        int pivot = arr[end];
                        int i = (begin-1);
                    
                        for (int j = begin; j < end; j++) {
                            if (arr[j] <= pivot) {
                                i++;
                    
                                int swapTemp = arr[i];
                                arr[i] = arr[j];
                                arr[j] = swapTemp;
                            }
                        }
                    
                        int swapTemp = arr[i+1];
                        arr[i+1] = arr[end];
                        arr[end] = swapTemp;
                    
                        return i+1;
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "[-5, -3, 1, 4, 5, 6, 23, 78, 3567367]");
    }
}
