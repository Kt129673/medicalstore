# Java Coding Round Preparation Guide
> Java 8 · Stream-first approach · Easy → Hard sequence

---

## Frequency Legend

| Rating | Meaning |
|--------|---------|
| ⭐⭐⭐⭐⭐ | Asked in almost every interview |
| ⭐⭐⭐⭐  | Very commonly asked |
| ⭐⭐⭐   | Moderately asked (senior / product roles) |
| ⭐⭐    | Niche / role-specific |

---

## Quick Reference Table

| # | Question | Category | Difficulty | Frequency |
|---|----------|----------|------------|-----------|
| 1 | Swap Two Numbers | Math | Easy | ⭐⭐⭐ |
| 2 | Reverse a String | String | Easy | ⭐⭐⭐⭐⭐ |
| 3 | Palindrome Check | String | Easy | ⭐⭐⭐⭐⭐ |
| 4 | Vowel & Consonant Count | String | Easy | ⭐⭐⭐⭐ |
| 5 | Factorial | Math | Easy | ⭐⭐⭐⭐ |
| 6 | Prime Number Check | Math | Easy | ⭐⭐⭐⭐ |
| 7 | Custom Exception | Java Core | Easy | ⭐⭐⭐ |
| 8 | Find Max / Min in List | Stream | Easy | ⭐⭐⭐ |
| 9 | Fibonacci Series | Math | Medium | ⭐⭐⭐⭐⭐ |
| 10 | Anagram Check | String | Medium | ⭐⭐⭐⭐ |
| 11 | Missing Number | Array | Medium | ⭐⭐⭐⭐⭐ |
| 12 | First Non-Repeated Character | String | Medium | ⭐⭐⭐⭐⭐ |
| 13 | Find Second Largest | Array | Medium | ⭐⭐⭐⭐ |
| 14 | Remove Duplicates | Array | Medium | ⭐⭐⭐⭐ |
| 15 | Filter Numbers Starting With '1' | Stream | Medium | ⭐⭐⭐⭐⭐ |
| 16 | Find Duplicates in a List | Stream | Medium | ⭐⭐⭐⭐⭐ |
| 17 | Grouping & Counting | Stream | Medium | ⭐⭐⭐⭐ |
| 18 | Sort Custom Employee Objects | Stream | Medium | ⭐⭐⭐⭐ |
| 19 | Binary Search | Algorithm | Medium | ⭐⭐⭐⭐ |
| 20 | Singleton Pattern | Design Pattern | Medium | ⭐⭐⭐⭐ |
| 21 | Rotate an Array | Array | Hard | ⭐⭐⭐ |
| 22 | Kadane's Algorithm | Array | Hard | ⭐⭐⭐⭐ |
| 23 | Longest Substring Without Repeating Chars | String | Hard | ⭐⭐⭐⭐⭐ |
| 24 | Matrix Rotation 90° | Matrix | Hard | ⭐⭐⭐ |
| 25 | LRU Cache (Thread-Safe) | Concurrent | Hard | ⭐⭐⭐⭐ |
| 26 | Create a Deadlock | Concurrent | Hard | ⭐⭐⭐ |
| 27 | Sum of Digits | Math | Easy | ⭐⭐⭐⭐ |
| 28 | Even / Odd Partition | Stream | Easy | ⭐⭐⭐⭐⭐ |
| 29 | Reverse Words in a Sentence | String | Easy | ⭐⭐⭐⭐ |
| 30 | Convert List to Uppercase | Stream | Easy | ⭐⭐⭐ |
| 31 | Count Words in a String | String | Easy | ⭐⭐⭐ |
| 32 | Armstrong Number Check | Math | Easy | ⭐⭐⭐ |
| 33 | Collectors.joining — Build Sentences | Stream | Easy | ⭐⭐⭐⭐ |
| 34 | FlatMap — Flatten List of Lists | Stream | Medium | ⭐⭐⭐⭐⭐ |
| 35 | Collectors.partitioningBy | Stream | Medium | ⭐⭐⭐⭐ |
| 36 | Collectors.toMap | Stream | Medium | ⭐⭐⭐⭐ |
| 37 | Optional — Null Safety | Java Core | Medium | ⭐⭐⭐⭐⭐ |
| 38 | String Compression | String | Medium | ⭐⭐⭐⭐ |
| 39 | Two Sum Problem | Array | Medium | ⭐⭐⭐⭐⭐ |
| 40 | Comparable vs Comparator | Java Core | Medium | ⭐⭐⭐⭐ |
| 41 | Sum & Average (Employee Salaries) | Stream | Medium | ⭐⭐⭐⭐ |
| 42 | Count Elements by Condition | Stream | Medium | ⭐⭐⭐ |
| 43 | Merge Two Sorted Arrays | Array | Medium | ⭐⭐⭐⭐ |
| 44 | CompletableFuture — Async Tasks | Concurrent | Hard | ⭐⭐⭐⭐ |
| 45 | ExecutorService / Thread Pool | Concurrent | Hard | ⭐⭐⭐⭐ |
| 46 | Producer-Consumer (BlockingQueue) | Concurrent | Hard | ⭐⭐⭐⭐ |
| 47 | Top-N Frequent Words | Stream | Hard | ⭐⭐⭐⭐ |
| 48 | Valid Parentheses | Stack | Hard | ⭐⭐⭐⭐⭐ |
| 49 | Immutable Class Design | Java Core | Hard | ⭐⭐⭐⭐ |
| 50 | Stream reduce() — Custom Aggregation | Stream | Hard | ⭐⭐⭐⭐ |

---

# EASY TIER

---

### Q1. Swap Two Numbers
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** Math

**Concept:**  
Swap two integer variables without using a third temporary variable. Two classic tricks exist: arithmetic (addition/subtraction) and bitwise XOR. This tests whether you know language-level tricks beyond the naive `temp` approach.

**Approach:**
- Arithmetic: `a = a + b`, then `b = a - b`, then `a = a - b`
- XOR: `a = a ^ b`, then `b = a ^ b`, then `a = a ^ b`
- Note: Streams are not applicable here — this is a single mutation, not a collection operation.

**Solution (Java 8):**
```java
public class SwapNumbers {
    public static void main(String[] args) {

        // ── Approach 1: Arithmetic ──────────────────────────────
        int a = 10, b = 20;
        System.out.println("Before: a=" + a + ", b=" + b);

        a = a + b;   // a = 30
        b = a - b;   // b = 30 - 20 = 10
        a = a - b;   // a = 30 - 10 = 20

        System.out.println("After (Arithmetic): a=" + a + ", b=" + b);
        // Output: a=20, b=10

        // ── Approach 2: XOR (no risk of integer overflow) ───────
        int x = 5, y = 7;
        x = x ^ y;   // x = 0101 ^ 0111 = 0010
        y = x ^ y;   // y = 0010 ^ 0111 = 0101 (original x)
        x = x ^ y;   // x = 0010 ^ 0101 = 0111 (original y)

        System.out.println("After (XOR): x=" + x + ", y=" + y);
        // Output: x=7, y=5
    }
}
```

**Key Interview Points:**
- Arithmetic swap can overflow for very large integers — XOR is safer.
- In real code, use a temp variable for clarity; these tricks are interview-specific.

---

### Q2. Reverse a String
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Reverse a string without using `StringBuilder.reverse()`. This checks your understanding of character streams and the `chars()` method introduced in Java 8. Every Java developer should know this cold.

**Approach:**
- Get the `IntStream` of characters via `str.chars()`
- Convert each `int` back to a `char` using `mapToObj(c -> String.valueOf((char) c))`
- Collect into a single string and reverse order by sorting in reverse index — OR collect and manually reverse

**Stream Solution (Java 8):**
```java
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReverseString {
    public static String reverseStream(String str) {
        return IntStream
            .rangeClosed(1, str.length())                     // indices 1..length
            .mapToObj(i -> String.valueOf(str.charAt(str.length() - i))) // pick from end
            .collect(Collectors.joining());                   // join all chars
    }

    // ── Alternative using StringBuilder (still no reverse()) ──
    public static String reverseTraditional(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(reverseStream("Hello"));       // olleH
        System.out.println(reverseTraditional("World"));  // dlroW
    }
}
```

**Key Interview Points:**
- `String.chars()` returns `IntStream`, not `Stream<Character>` — interviewers often ask why.
- Always mention time complexity: O(n).

---

### Q3. Palindrome Check
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
A palindrome reads the same forwards and backwards (e.g., "madam", "racecar"). The stream approach compares characters at mirrored indices using `IntStream.range`. Common in screening rounds.

**Approach:**
- Use `IntStream.range(0, length/2)` to iterate over only the first half
- At each index `i`, compare `str.charAt(i)` with `str.charAt(length - 1 - i)`
- If all pairs match, it is a palindrome — use `allMatch`

**Stream Solution (Java 8):**
```java
import java.util.stream.IntStream;

public class PalindromeCheck {
    public static boolean isPalindrome(String str) {
        String s = str.toLowerCase().replaceAll("[^a-z0-9]", ""); // normalize
        int len = s.length();
        return IntStream
            .range(0, len / 2)                         // only half needed
            .allMatch(i -> s.charAt(i) == s.charAt(len - 1 - i)); // mirror compare
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome("racecar"));   // true
        System.out.println(isPalindrome("A man a plan a canal Panama")); // true
        System.out.println(isPalindrome("hello"));     // false
    }
}
```

**Key Interview Points:**
- Always normalize (lowercase, strip spaces/punctuation) before checking — interviewers test edge cases.
- `allMatch` short-circuits on the first mismatch: efficient, O(n/2).

---

### Q4. Vowel & Consonant Count
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Count vowels and consonants in a string. Stream's `filter` makes this clean — one stream for vowels, one for consonants (letters that aren't vowels). Tests basic `chars()` usage.

**Approach:**
- Convert string to lowercase
- Stream all chars and filter only alphabetic characters
- `filter` for vowels using `"aeiou".indexOf(c) >= 0`
- Consonants = total letters minus vowels

**Stream Solution (Java 8):**
```java
import java.util.stream.IntStream;

public class VowelConsonantCount {
    public static void main(String[] args) {
        String str = "Hello World";
        String lower = str.toLowerCase();

        // Count vowels: filter chars that are in "aeiou"
        long vowels = lower.chars()
            .filter(c -> "aeiou".indexOf(c) >= 0)       // is it a vowel?
            .count();

        // Count consonants: filter letters that are NOT vowels
        long consonants = lower.chars()
            .filter(c -> c >= 'a' && c <= 'z')           // is it a letter?
            .filter(c -> "aeiou".indexOf(c) < 0)         // and NOT a vowel?
            .count();

        System.out.println("Vowels: " + vowels);         // 3
        System.out.println("Consonants: " + consonants); // 7
    }
}
```

**Key Interview Points:**
- Two separate stream pipelines is fine — don't force both into one for readability.
- Ask the interviewer: "Should spaces and numbers be counted?" — shows awareness of edge cases.

---

### Q5. Factorial
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** Mathematical Logic

**Concept:**  
Factorial of n (n!) is the product of all integers from 1 to n. Java 8's `LongStream` with `reduce` gives an elegant single-line solution. Recursive approach is also expected.

**Approach:**
- Stream: `LongStream.rangeClosed(1, n)` generates all values 1..n
- `reduce(1L, Math::multiplyExact)` multiplies them all together
- Recursive: `n * factorial(n-1)`, base case `n == 0 → 1`

**Stream Solution (Java 8):**
```java
import java.util.stream.LongStream;

public class Factorial {
    // ── Stream approach ─────────────────────────────────────────
    public static long factorialStream(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative input");
        if (n == 0) return 1L;
        return LongStream
            .rangeClosed(1, n)                  // 1, 2, 3, ..., n
            .reduce(1L, Math::multiplyExact);   // multiply all together
    }

    // ── Recursive approach ───────────────────────────────────────
    public static long factorialRecursive(int n) {
        return (n <= 1) ? 1 : n * factorialRecursive(n - 1);
    }

    public static void main(String[] args) {
        System.out.println(factorialStream(5));       // 120
        System.out.println(factorialRecursive(10));   // 3628800
    }
}
```

**Key Interview Points:**
- `Math::multiplyExact` throws `ArithmeticException` on overflow — safer than `*`.
- For large n, suggest `BigInteger` — shows awareness of practical limits.

---

### Q6. Prime Number Check
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** Mathematical Logic

**Concept:**  
A prime number is divisible only by 1 and itself. The key optimization is to check divisors only up to √n. Java 8's `IntStream.rangeClosed` with `noneMatch` makes this a clean one-liner.

**Approach:**
- Numbers ≤ 1 are not prime — handle as edge case
- Check if any number in range `[2, √n]` divides `n` evenly
- `noneMatch(i -> n % i == 0)` — if none divide it, it is prime

**Stream Solution (Java 8):**
```java
import java.util.stream.IntStream;

public class PrimeCheck {
    public static boolean isPrime(int n) {
        if (n <= 1) return false;                    // 0, 1 are not prime
        if (n <= 3) return true;                     // 2, 3 are prime
        if (n % 2 == 0) return false;                // quick even check

        return IntStream
            .rangeClosed(2, (int) Math.sqrt(n))      // check 2 to √n
            .noneMatch(i -> n % i == 0);             // none should divide n
    }

    public static void main(String[] args) {
        System.out.println(isPrime(7));    // true
        System.out.println(isPrime(15));   // false (3×5)
        System.out.println(isPrime(97));   // true
    }
}
```

**Key Interview Points:**
- Checking up to √n reduces complexity from O(n) to O(√n) — always mention this.
- `noneMatch` short-circuits on the first divisor found — efficient.

---

### Q7. Custom Exception
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** Java Core

**Concept:**  
Custom exceptions let you represent domain-specific error conditions (e.g., `InsufficientStockException`). They extend `RuntimeException` (unchecked) or `Exception` (checked). Streams are not applicable here — this is class hierarchy design.

**Approach:**
- Extend `RuntimeException` for unchecked, `Exception` for checked
- Add a constructor that passes a message to `super(message)`
- Throw it with `throw new YourException("message")`
- Catch it like any standard exception

**Solution (Java 8):**
```java
// ── Step 1: Define the custom exception ──────────────────────────
class InsufficientStockException extends RuntimeException {
    private final String medicineName;

    public InsufficientStockException(String medicineName, int requested) {
        super("Insufficient stock for '" + medicineName
              + "'. Requested: " + requested);
        this.medicineName = medicineName;
    }

    public String getMedicineName() {
        return medicineName;
    }
}

// ── Step 2: Use it ────────────────────────────────────────────────
public class CustomExceptionDemo {
    static void dispense(String medicine, int stock, int requested) {
        if (requested > stock) {
            throw new InsufficientStockException(medicine, requested);
        }
        System.out.println("Dispensed " + requested + " units of " + medicine);
    }

    public static void main(String[] args) {
        try {
            dispense("Paracetamol", 50, 100);  // will throw
        } catch (InsufficientStockException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Medicine: " + e.getMedicineName());
        }
    }
}
// Output:
// Error: Insufficient stock for 'Paracetamol'. Requested: 100
// Medicine: Paracetamol
```

**Key Interview Points:**
- Prefer `RuntimeException` (unchecked) for programming errors; `Exception` (checked) for recoverable business conditions.
- Always add contextual fields to custom exceptions — not just a message string.

---

### Q8. Find Max / Min in a List
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Finding the max or min value in a collection is a perfect fit for Streams. Java 8 provides both `stream().max()` and `mapToInt().max()` variants. A good warm-up question before moving to complex stream problems.

**Approach:**
- Use `stream().max(Comparator.naturalOrder())` — returns `Optional<T>`
- Or use `mapToInt(Integer::intValue).max()` — returns `OptionalInt`
- Always handle the `Optional` with `.orElseThrow()` or `.orElse()`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class MaxMinStream {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(3, 1, 7, 2, 9, 5, 4);

        // ── Max using Comparator ──────────────────────────────────
        Optional<Integer> max = numbers.stream()
            .max(Comparator.naturalOrder());            // finds maximum
        System.out.println("Max: " + max.orElseThrow()); // 9

        // ── Min using Comparator ──────────────────────────────────
        Optional<Integer> min = numbers.stream()
            .min(Comparator.naturalOrder());            // finds minimum
        System.out.println("Min: " + min.orElseThrow()); // 1

        // ── Primitive stream variant (slightly faster) ────────────
        OptionalInt maxPrimitive = numbers.stream()
            .mapToInt(Integer::intValue)
            .max();
        System.out.println("Max (primitive): " + maxPrimitive.getAsInt()); // 9
    }
}
```

**Key Interview Points:**
- `max()`/`min()` return `Optional` — explain why (empty list has no max).
- `mapToInt()` avoids boxing/unboxing — mention this for performance-aware answers.

---

# MEDIUM TIER

---

### Q9. Fibonacci Series
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Mathematical Logic

**Concept:**  
The Fibonacci series is 0, 1, 1, 2, 3, 5, 8, 13... where each number is the sum of the previous two. Java 8's `Stream.iterate` can generate this lazily. Almost certain to appear in any Java round.

**Approach:**
- Stream: `Stream.iterate` with a seed pair `{0, 1}` and a lambda that shifts the pair
- Map out the first element of each pair — that is the Fibonacci number
- `limit(n)` controls how many terms to generate
- Recursive: `fib(n) = fib(n-1) + fib(n-2)`, base cases `n==0→0`, `n==1→1`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.stream.Stream;

public class Fibonacci {
    // ── Stream approach ──────────────────────────────────────────
    public static void fibonacciStream(int n) {
        Stream.iterate(new long[]{0, 1},
                       f -> new long[]{f[1], f[0] + f[1]}) // shift: [b, a+b]
              .limit(n)                                      // first n terms
              .map(f -> f[0])                                // first element = current fib
              .forEach(System.out::println);
    }

    // ── Iterative approach ───────────────────────────────────────
    public static void fibonacciIterative(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            System.out.print(a + " ");
            long temp = a + b;
            a = b;
            b = temp;
        }
    }

    // ── Recursive approach (not efficient for large n) ───────────
    public static long fibRecursive(int n) {
        if (n <= 1) return n;
        return fibRecursive(n - 1) + fibRecursive(n - 2);
    }

    public static void main(String[] args) {
        System.out.println("Stream (first 8 terms):");
        fibonacciStream(8);           // 0 1 1 2 3 5 8 13

        System.out.println("\nIterative:");
        fibonacciIterative(8);        // 0 1 1 2 3 5 8 13

        System.out.println("\nRecursive fib(7): " + fibRecursive(7)); // 13
    }
}
```

**Key Interview Points:**
- Recursive is elegant but O(2^n) time — always mention memoization or dynamic programming.
- `Stream.iterate` is lazy: it generates values only when consumed — memory efficient.

---

### Q10. Anagram Check
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Two strings are anagrams if they contain exactly the same characters in the same frequencies (e.g., "silent" and "listen"). Stream's `chars().sorted()` approach is clean and interview-ready.

**Approach:**
- Normalize both strings: lowercase, remove spaces
- Sort the characters of both strings
- If the sorted arrays are equal, they are anagrams
- Stream variant: collect sorted chars into a string and use `.equals()`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;

public class AnagramCheck {
    // ── Stream approach ──────────────────────────────────────────
    public static boolean isAnagramStream(String s1, String s2) {
        String normalize = s1.toLowerCase().replaceAll("\\s", "");
        String normalize2 = s2.toLowerCase().replaceAll("\\s", "");

        if (normalize.length() != normalize2.length()) return false;

        // Sort chars of both strings and compare
        char[] arr1 = normalize.chars()
            .sorted()                            // sort int values of chars
            .collect(StringBuilder::new,
                     (sb, c) -> sb.append((char) c),
                     StringBuilder::append)
            .toString()
            .toCharArray();

        char[] arr2 = normalize2.chars()
            .sorted()
            .collect(StringBuilder::new,
                     (sb, c) -> sb.append((char) c),
                     StringBuilder::append)
            .toString()
            .toCharArray();

        return Arrays.equals(arr1, arr2);
    }

    // ── Cleaner stream variant ────────────────────────────────────
    public static boolean isAnagram(String s1, String s2) {
        String a = sorted(s1);
        String b = sorted(s2);
        return a.equals(b);
    }

    private static String sorted(String s) {
        return s.toLowerCase()
                .replaceAll("\\s", "")
                .chars()
                .sorted()
                .collect(StringBuilder::new,
                         (sb, c) -> sb.append((char) c),
                         StringBuilder::append)
                .toString();
    }

    public static void main(String[] args) {
        System.out.println(isAnagram("silent", "listen"));  // true
        System.out.println(isAnagram("hello", "bello"));    // false
        System.out.println(isAnagram("Astronomer", "Moon starer")); // true
    }
}
```

**Key Interview Points:**
- Sorting approach is O(n log n); a `HashMap` frequency counter is O(n) — mention both.
- Always ask "are spaces counted?" and "is it case-sensitive?" before coding.

---

### Q11. Missing Number
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Given an array containing n-1 distinct numbers from 1 to n, find the missing one. The classic trick is: sum of 1..n is `n*(n+1)/2`; subtract the array's actual sum to get the missing value. Streams make this a two-liner.

**Approach:**
- Find `n` = array length + 1
- Expected sum = `n*(n+1)/2` — or use `IntStream.rangeClosed(1,n).sum()`
- Actual sum = `Arrays.stream(arr).sum()`
- Missing = expected − actual

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.stream.IntStream;

public class MissingNumber {
    public static int findMissing(int[] arr) {
        int n = arr.length + 1;                        // array has n-1 elements

        int expectedSum = IntStream
            .rangeClosed(1, n)                         // 1 to n
            .sum();                                    // n*(n+1)/2

        int actualSum = Arrays.stream(arr).sum();      // sum of given array

        return expectedSum - actualSum;                // difference = missing
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 4, 5, 6};                  // 3 is missing
        System.out.println("Missing: " + findMissing(arr)); // 3

        int[] arr2 = {2, 3, 4, 5};                    // 1 is missing
        System.out.println("Missing: " + findMissing(arr2)); // 1
    }
}
```

**Key Interview Points:**
- XOR approach also works: XOR all indices 1..n with all array elements — missing number remains.
- Always clarify: is the array sorted? Does it start from 1? Are there negatives?

---

### Q12. First Non-Repeated Character
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Find the first character in a string that appears only once (e.g., in "aabbcd", the answer is 'c'). The stream approach uses `Collectors.groupingBy` to count frequencies, then `filter` to find the first with count == 1.

**Approach:**
- Use a `LinkedHashMap` to preserve insertion order (critical for "first")
- `Collectors.groupingBy(identity(), LinkedHashMap::new, counting())` gives ordered frequency map
- Stream `.entrySet()`, filter `value == 1`, `.findFirst()` gives the answer

**Stream Solution (Java 8):**
```java
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FirstNonRepeated {
    public static Character firstNonRepeated(String str) {
        return str.chars()
            .mapToObj(c -> (char) c)                        // IntStream → Stream<Character>
            .collect(Collectors.groupingBy(
                Function.identity(),                         // group by the char itself
                LinkedHashMap::new,                          // preserve insertion order!
                Collectors.counting()))                      // count occurrences
            .entrySet().stream()
            .filter(e -> e.getValue() == 1)                 // keep count==1 entries
            .map(Map.Entry::getKey)
            .findFirst()                                     // first such character
            .orElse(null);                                   // null if all repeat
    }

    public static void main(String[] args) {
        System.out.println(firstNonRepeated("aabbcd"));     // c
        System.out.println(firstNonRepeated("swiss"));      // w
        System.out.println(firstNonRepeated("aabb"));       // null
    }
}
```

**Key Interview Points:**
- `LinkedHashMap::new` is the critical detail — without it, order is not guaranteed.
- Many candidates miss this and use `HashMap` — knowing this difference stands out.

---

### Q13. Find Second Largest
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Find the second largest distinct element in an integer array. Streams make this elegant with `distinct().sorted()` in reverse order, then `skip(1).findFirst()`.

**Approach:**
- Remove duplicates with `distinct()`
- Sort in reverse order (largest first) with `sorted(Comparator.reverseOrder())`
- Skip the first (largest) element with `skip(1)`
- Take the next one with `findFirst()`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SecondLargest {
    public static Optional<Integer> secondLargest(List<Integer> list) {
        return list.stream()
            .distinct()                                  // remove duplicates
            .sorted(Comparator.reverseOrder())           // 9, 7, 5, 3, 1
            .skip(1)                                     // skip largest (9)
            .findFirst();                                // return next = 7
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(5, 3, 9, 7, 9, 1, 3);
        System.out.println("Second Largest: "
            + secondLargest(numbers).orElse(-1));           // 7

        List<Integer> allSame = Arrays.asList(5, 5, 5);
        System.out.println("Second Largest: "
            + secondLargest(allSame).orElse(-1));           // -1 (no second largest)
    }
}
```

**Key Interview Points:**
- `distinct()` is important — `{5, 5, 3}` without it would return `5` as second largest.
- `sorted()` on the full stream is O(n log n) — mention that a two-pass O(n) approach exists for performance-critical code.

---

### Q14. Remove Duplicates
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Remove duplicate elements from a list. Streams make this trivially easy with `distinct()`. For sorted arrays, interviewers may ask for an in-place approach — both covered here.

**Approach:**
- Stream: `stream().distinct().collect(Collectors.toList())`
- For a sorted int array without extra memory: two-pointer approach
- Both approaches should be known

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveDuplicates {
    // ── Stream approach (for List) ────────────────────────────────
    public static List<Integer> removeDuplicatesStream(List<Integer> list) {
        return list.stream()
            .distinct()                                  // removes duplicates
            .collect(Collectors.toList());
    }

    // ── Two-pointer for sorted int array (no extra memory) ───────
    public static int removeDuplicatesInPlace(int[] arr) {
        if (arr.length == 0) return 0;
        int uniqueIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != arr[uniqueIndex]) {           // found a new unique element
                uniqueIndex++;
                arr[uniqueIndex] = arr[i];              // place it next
            }
        }
        return uniqueIndex + 1;                         // count of unique elements
    }

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 2, 3, 4, 4, 5);
        System.out.println(removeDuplicatesStream(list));   // [1, 2, 3, 4, 5]

        int[] arr = {1, 1, 2, 3, 3, 4, 5};
        int count = removeDuplicatesInPlace(arr);
        System.out.print("Unique elements: ");
        for (int i = 0; i < count; i++) System.out.print(arr[i] + " ");
        // 1 2 3 4 5
    }
}
```

**Key Interview Points:**
- `distinct()` uses `equals()` and `hashCode()` — for custom objects, these must be overridden.
- For sorted arrays, O(1) space two-pointer is the optimal expected answer.

---

### Q15. Filter Numbers Starting With '1'
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Given a list of integers, return only those that start with the digit '1' (e.g., 1, 10, 11, 100). This is a classic Java 8 Stream warm-up combining `filter` and `String.valueOf()`. Almost certainly asked in Java 8 screening rounds.

**Approach:**
- Convert each number to a String with `String.valueOf(n)`
- `filter` to check if it `startsWith("1")`
- Collect back to a list

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterStartsWithOne {
    public static List<Integer> filterStartingWithOne(List<Integer> numbers) {
        return numbers.stream()
            .filter(n -> String.valueOf(n).startsWith("1")) // convert to String, check prefix
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(10, 25, 1, 100, 37, 15, 200, 11);
        System.out.println(filterStartingWithOne(numbers));
        // [10, 1, 100, 15, 11]

        // ── Bonus: also print their squares ──────────────────────
        numbers.stream()
            .filter(n -> String.valueOf(n).startsWith("1"))
            .map(n -> n * n)                             // square each
            .forEach(System.out::println);               // 100, 1, 10000, 225, 121
    }
}
```

**Key Interview Points:**
- This is usually a gateway question — if you answer it quickly with Streams, the interviewer moves to harder stream questions.
- Extend the answer: "I can also chain `.map()`, `.sorted()` etc. if needed."

---

### Q16. Find Duplicates in a List
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Find elements that appear more than once in a list. Use `Collectors.groupingBy` to count occurrences, then filter. One of the most commonly asked Java 8 Stream questions.

**Approach:**
- Use `Collectors.groupingBy(identity(), counting())` to get a frequency map
- Stream the map's entries and filter where `value > 1`
- Collect the keys (duplicate elements)

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FindDuplicates {
    public static Set<Integer> findDuplicates(List<Integer> list) {
        return list.stream()
            .collect(Collectors.groupingBy(
                Function.identity(),             // group each element by itself
                Collectors.counting()))          // count occurrences
            .entrySet().stream()
            .filter(e -> e.getValue() > 1)      // keep only those appearing > 1 time
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    // ── Alternative: using a HashSet to track seen elements ──────
    public static List<Integer> findDuplicatesAlt(List<Integer> list) {
        Set<Integer> seen = new java.util.HashSet<>();
        return list.stream()
            .filter(n -> !seen.add(n))           // add() returns false if already present
            .distinct()
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5);
        System.out.println("Duplicates: " + findDuplicates(numbers));     // [2, 3]
        System.out.println("Duplicates: " + findDuplicatesAlt(numbers));  // [2, 3]
    }
}
```

**Key Interview Points:**
- The `Set::add` trick (`!seen.add(n)`) is a clever single-pass approach — interviewers love it.
- Know both approaches: frequency map for counts, `Set.add()` for just existence of duplicates.

---

### Q17. Grouping & Counting Characters / Words
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Count the frequency of each character (or word) in a string — a real-world use case for `Collectors.groupingBy`. This directly demonstrates your understanding of Java 8 Collectors.

**Approach:**
- For characters: `chars().mapToObj(c -> (char)c)` then `groupingBy(identity(), counting())`
- For words: `split("\\s+")` then `Arrays.stream()` then same groupingBy
- Use `LinkedHashMap` if order matters

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupingAndCounting {
    public static void main(String[] args) {

        // ── Character frequency ───────────────────────────────────
        String str = "programming";
        Map<Character, Long> charFreq = str.chars()
            .mapToObj(c -> (char) c)                     // int → Character
            .collect(Collectors.groupingBy(
                Function.identity(),                      // group by char itself
                LinkedHashMap::new,                       // preserve order
                Collectors.counting()));                  // count each group

        System.out.println("Char frequency: " + charFreq);
        // {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}

        // ── Word frequency ────────────────────────────────────────
        String sentence = "java is great java is fun";
        Map<String, Long> wordFreq = Arrays.stream(sentence.split("\\s+"))
            .collect(Collectors.groupingBy(
                Function.identity(),                      // group by word
                Collectors.counting()));

        System.out.println("Word frequency: " + wordFreq);
        // {java=2, is=2, great=1, fun=1}

        // ── Sorted by frequency (most common first) ───────────────
        wordFreq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> System.out.println(e.getKey() + " → " + e.getValue()));
    }
}
```

**Key Interview Points:**
- `Collectors.groupingBy` has three overloads — know all three: key extractor, map factory, downstream collector.
- Sorting by value (frequency) is often asked as a follow-up.

---

### Q18. Sort Custom Employee Objects
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Sort a list of `Employee` objects by salary descending, then by name ascending for equal salaries. Uses `Comparator.comparing().thenComparing()` — a direct test of Java 8 Comparator chaining.

**Approach:**
- Define an `Employee` class with `name`, `salary`, `department`
- Use `Comparator.comparingInt(Employee::getSalary).reversed()` for descending salary
- Chain `.thenComparing(Employee::getName)` for the secondary sort

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// ── Employee POJO (reused across examples) ────────────────────────
class Employee {
    private String name;
    private int salary;
    private String department;

    public Employee(String name, int salary, String department) {
        this.name = name; this.salary = salary; this.department = department;
    }
    public String getName()       { return name; }
    public int getSalary()        { return salary; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return name + " (" + department + ") - ₹" + salary;
    }
}

public class SortEmployees {
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 75000, "IT"),
            new Employee("Bob",   50000, "HR"),
            new Employee("Carol", 75000, "Finance"),
            new Employee("Dave",  60000, "IT"),
            new Employee("Eve",   50000, "HR")
        );

        // ── Sort: salary DESC, then name ASC ──────────────────────
        List<Employee> sorted = employees.stream()
            .sorted(
                Comparator.comparingInt(Employee::getSalary)
                          .reversed()                       // salary high → low
                          .thenComparing(Employee::getName) // then A → Z by name
            )
            .collect(Collectors.toList());

        sorted.forEach(System.out::println);
        // Alice (IT)      - ₹75000
        // Carol (Finance) - ₹75000
        // Dave  (IT)      - ₹60000
        // Bob   (HR)      - ₹50000
        // Eve   (HR)      - ₹50000

        // ── Group by department ───────────────────────────────────
        System.out.println("\nBy department:");
        employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment))
            .forEach((dept, emps) -> System.out.println(dept + ": " + emps));
    }
}
```

**Key Interview Points:**
- `.reversed()` should be called on the first comparator, before `.thenComparing()` — a common mistake is reversing the entire chain.
- `Comparator.comparing` for `String`, `comparingInt/comparingLong/comparingDouble` for primitives.

---

### Q19. Binary Search
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Algorithms

**Concept:**  
Binary search finds an element in a sorted array in O(log n) by repeatedly halving the search space. Both iterative and recursive variants are expected. Streams are not applicable here — stream operations don't maintain index state between elements.

**Approach:**
- Set `low=0`, `high=arr.length-1`
- `mid = low + (high - low) / 2` (avoids integer overflow)
- If `arr[mid] == target`, return `mid`
- If `arr[mid] < target`, set `low = mid + 1`; else set `high = mid - 1`

**Solution (Java 8):**
```java
public class BinarySearch {
    // ── Iterative ─────────────────────────────────────────────────
    public static int binarySearchIterative(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;       // avoids overflow vs (low+high)/2
            if (arr[mid] == target)   return mid;   // found
            else if (arr[mid] < target) low = mid + 1;  // target is in right half
            else                      high = mid - 1;  // target is in left half
        }
        return -1;                                  // not found
    }

    // ── Recursive ─────────────────────────────────────────────────
    public static int binarySearchRecursive(int[] arr, int low, int high, int target) {
        if (low > high) return -1;
        int mid = low + (high - low) / 2;
        if (arr[mid] == target)    return mid;
        if (arr[mid] < target)     return binarySearchRecursive(arr, mid + 1, high, target);
        return                            binarySearchRecursive(arr, low, mid - 1, target);
    }

    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};

        System.out.println(binarySearchIterative(arr, 23));     // 5 (index)
        System.out.println(binarySearchIterative(arr, 100));    // -1

        System.out.println(binarySearchRecursive(arr, 0, arr.length - 1, 56)); // 7
    }
}
// Note: Streams don't apply here — binary search requires direct index access
// and bidirectional navigation which stream pipelines don't support.
```

**Key Interview Points:**
- Use `low + (high - low) / 2` not `(low + high) / 2` — prevents integer overflow.
- Time: O(log n), Space: O(1) iterative, O(log n) recursive (call stack).

---

### Q20. Singleton Pattern (Thread-Safe)
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Design Patterns

**Concept:**  
Singleton ensures only one instance of a class exists throughout the application. Thread safety is the main challenge. Three approaches: double-checked locking, `enum`, and Bill Pugh (holder class) — know all three.

**Approach:**
- **Double-checked locking**: `volatile` field + `synchronized` block only on first creation
- **Enum Singleton**: JVM guarantees single instance, serialization-safe, simplest
- **Bill Pugh**: lazy initialization using a static inner holder class — no synchronization overhead

**Solution (Java 8):**
```java
// ── Approach 1: Double-Checked Locking ────────────────────────────
class SingletonDCL {
    private static volatile SingletonDCL instance;   // volatile prevents partial init

    private SingletonDCL() { }                        // private constructor

    public static SingletonDCL getInstance() {
        if (instance == null) {                        // first check (no lock)
            synchronized (SingletonDCL.class) {        // lock only if null
                if (instance == null) {                // second check (inside lock)
                    instance = new SingletonDCL();
                }
            }
        }
        return instance;
    }
}

// ── Approach 2: Enum Singleton (RECOMMENDED — Josh Bloch) ─────────
enum SingletonEnum {
    INSTANCE;                                          // JVM ensures single instance

    public void doSomething() {
        System.out.println("Singleton via Enum");
    }
}

// ── Approach 3: Bill Pugh (Initialization-on-Demand Holder) ───────
class SingletonBillPugh {
    private SingletonBillPugh() { }

    // Inner class loaded only when getInstance() is first called
    private static class Holder {
        private static final SingletonBillPugh INSTANCE = new SingletonBillPugh();
    }

    public static SingletonBillPugh getInstance() {
        return Holder.INSTANCE;                        // lazy, thread-safe, no sync overhead
    }
}

public class SingletonDemo {
    public static void main(String[] args) {
        SingletonDCL s1 = SingletonDCL.getInstance();
        SingletonDCL s2 = SingletonDCL.getInstance();
        System.out.println(s1 == s2);                  // true (same instance)

        SingletonEnum.INSTANCE.doSomething();
    }
}
```

**Key Interview Points:**
- `volatile` is mandatory in DCL — without it, another thread can see a partially constructed object.
- Enum Singleton is preferred by Effective Java (Joshua Bloch) — always mention this.

---

# HARD TIER

---

### Q21. Rotate an Array
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Rotate an array by `k` positions. For example, `[1,2,3,4,5]` rotated right by 2 becomes `[4,5,1,2,3]`. The reversal algorithm does this in O(n) time and O(1) space. Streams are used for output but not for the rotation itself.

**Approach:**
- Right rotate by `k`: reverse the entire array, reverse first `k` elements, reverse remaining
- Normalize `k = k % n` to handle cases where `k >= n`
- Three reversal operations total

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.stream.IntStream;

public class RotateArray {
    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++; end--;
        }
    }

    // ── Right rotate by k ─────────────────────────────────────────
    public static void rotateRight(int[] arr, int k) {
        int n = arr.length;
        k = k % n;                      // handle k >= n
        if (k == 0) return;

        reverse(arr, 0, n - 1);         // step 1: reverse all
        reverse(arr, 0, k - 1);         // step 2: reverse first k elements
        reverse(arr, k, n - 1);         // step 3: reverse remaining elements
    }

    // ── Left rotate by k ──────────────────────────────────────────
    public static void rotateLeft(int[] arr, int k) {
        int n = arr.length;
        k = k % n;
        if (k == 0) return;

        reverse(arr, 0, k - 1);         // reverse first k elements
        reverse(arr, k, n - 1);         // reverse remaining
        reverse(arr, 0, n - 1);         // reverse all
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        System.out.println("Original: " + Arrays.toString(arr));

        rotateRight(arr, 2);
        System.out.println("Right by 2: " + Arrays.toString(arr)); // [4, 5, 1, 2, 3]

        int[] arr2 = {1, 2, 3, 4, 5};
        rotateLeft(arr2, 2);
        System.out.println("Left by 2: " + Arrays.toString(arr2));  // [3, 4, 5, 1, 2]
    }
}
// Stream note: Streams don't support in-place index mutations.
// Use IntStream.of(arr).forEach() for printing, but the rotation itself is traditional.
```

**Key Interview Points:**
- The reversal algorithm is O(n) time, O(1) space — state both explicitly.
- Simpler but O(n) space alternative: create a new array with `new_arr[i] = arr[(i - k + n) % n]`.

---

### Q22. Kadane's Algorithm
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Arrays / Dynamic Programming

**Concept:**  
Find the maximum sum contiguous subarray in an array containing negative numbers (e.g., `[-2,1,-3,4,-1,2,1,-5,4]` → answer is `[4,-1,2,1]` with sum `6`). Kadane's algorithm solves this in O(n).

**Approach:**
- Maintain `currentMax` (max sum ending at current index) and `globalMax` (overall max)
- At each element: `currentMax = max(element, currentMax + element)`
- Update `globalMax = max(globalMax, currentMax)`
- If `currentMax < 0`, reset it — starting fresh is better

**Solution (Java 8):**
```java
import java.util.Arrays;

public class KadanesAlgorithm {
    // ── Kadane's: returns max subarray sum ────────────────────────
    public static int maxSubarraySum(int[] arr) {
        int currentMax = arr[0];
        int globalMax  = arr[0];

        for (int i = 1; i < arr.length; i++) {
            // extend current subarray OR start fresh from arr[i]
            currentMax = Math.max(arr[i], currentMax + arr[i]);
            globalMax  = Math.max(globalMax, currentMax);
        }
        return globalMax;
    }

    // ── Extended: also track start and end indices ────────────────
    public static int[] maxSubarrayWithIndices(int[] arr) {
        int currentMax = arr[0], globalMax = arr[0];
        int start = 0, end = 0, tempStart = 0;

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > currentMax + arr[i]) {
                currentMax = arr[i];
                tempStart  = i;              // new subarray starts here
            } else {
                currentMax += arr[i];
            }
            if (currentMax > globalMax) {
                globalMax = currentMax;
                start = tempStart;           // record start index
                end   = i;                   // record end index
            }
        }
        return Arrays.copyOfRange(arr, start, end + 1);
    }

    public static void main(String[] args) {
        int[] arr = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("Max Sum: " + maxSubarraySum(arr));          // 6
        System.out.println("Subarray: " + Arrays.toString(maxSubarrayWithIndices(arr)));
        // [4, -1, 2, 1]
    }
}
// Stream reduce() note: Stream.reduce can compute a running sum but cannot easily
// reset the accumulator to 0 (like re-starting a subarray on negative), so
// the traditional loop is the canonical solution.
```

**Key Interview Points:**
- This is a classic Dynamic Programming problem — Kadane's is essentially DP with O(1) space.
- Always handle all-negative arrays: the answer is the single largest element, not 0.

---

### Q23. Longest Substring Without Repeating Characters
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Find the length of the longest substring with no repeated characters (e.g., "abcabcbb" → 3, "pwwkew" → 3). Uses the sliding window pattern with a `HashSet` or `HashMap`. Streams cannot solve this because the sliding window requires mutable state and dynamic resizing.

**Approach:**
- Use two pointers `left` and `right` (the window boundaries)
- Use a `HashSet` to track characters in the current window
- If `str.charAt(right)` is already in the set, shrink from `left`
- Track `maxLength = max(maxLength, right - left + 1)`

**Solution (Java 8):**
```java
import java.util.HashMap;
import java.util.Map;

public class LongestSubstringNoRepeat {
    // ── Approach 1: HashSet sliding window ────────────────────────
    public static int lengthOfLongestSubstring(String s) {
        java.util.Set<Character> window = new java.util.HashSet<>();
        int left = 0, maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // Shrink window from left until current char is not in window
            while (window.contains(c)) {
                window.remove(s.charAt(left));
                left++;
            }
            window.add(c);
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }

    // ── Approach 2: HashMap for O(n) (jump left pointer directly) ─
    public static int lengthOptimized(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>(); // char → last index
        int left = 0, maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                left = lastSeen.get(c) + 1;              // jump left past the duplicate
            }
            lastSeen.put(c, right);                      // update last seen index
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }

    public static void main(String[] args) {
        System.out.println(lengthOfLongestSubstring("abcabcbb")); // 3 (abc)
        System.out.println(lengthOfLongestSubstring("bbbbb"));    // 1 (b)
        System.out.println(lengthOptimized("pwwkew"));            // 3 (wke)
        System.out.println(lengthOptimized(""));                   // 0
    }
}
// Stream note: Streams cannot maintain a mutable sliding window (two-pointer state).
// This is a state-machine problem requiring direct index control.
```

**Key Interview Points:**
- HashMap approach jumps the `left` pointer directly — O(n) guaranteed, no inner while loop.
- Ask the interviewer: "Does it include spaces and special characters?" Small clarification shows maturity.

---

### Q24. Matrix Rotation 90° Clockwise
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐  
**Category:** Arrays / Matrix

**Concept:**  
Rotate an NxN matrix 90 degrees clockwise in-place. The trick: first transpose the matrix (swap `[i][j]` with `[j][i]`), then reverse each row. Two O(n²) passes, O(1) space.

**Approach:**
- **Transpose**: for `i` in `0..n`, for `j` in `i..n`, swap `matrix[i][j]` and `matrix[j][i]`
- **Reverse each row**: for each row, reverse its elements
- Combined effect = 90° clockwise rotation

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.stream.IntStream;

public class MatrixRotation {
    // ── Transpose: swap matrix[i][j] ↔ matrix[j][i] ──────────────
    private static void transpose(int[][] m) {
        int n = m.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {   // j starts at i+1 to avoid double swap
                int temp  = m[i][j];
                m[i][j]   = m[j][i];
                m[j][i]   = temp;
            }
        }
    }

    // ── Reverse each row ──────────────────────────────────────────
    private static void reverseRows(int[][] m) {
        for (int[] row : m) {
            int left = 0, right = row.length - 1;
            while (left < right) {
                int temp = row[left]; row[left] = row[right]; row[right] = temp;
                left++; right--;
            }
        }
    }

    public static void rotate90Clockwise(int[][] m) {
        transpose(m);    // step 1: transpose
        reverseRows(m);  // step 2: reverse rows → 90° clockwise
    }

    // ── IntStream for pretty printing ─────────────────────────────
    private static void printMatrix(int[][] m) {
        IntStream.range(0, m.length)
            .forEach(i -> System.out.println(Arrays.toString(m[i])));
    }

    public static void main(String[] args) {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        System.out.println("Original:");
        printMatrix(matrix);

        rotate90Clockwise(matrix);

        System.out.println("Rotated 90° Clockwise:");
        printMatrix(matrix);
        // [7, 4, 1]
        // [8, 5, 2]
        // [9, 6, 3]
    }
}
```

**Key Interview Points:**
- Transpose + reverse rows = 90° clockwise. Reverse rows first + transpose = 90° counter-clockwise.
- This only works for square (NxN) matrices — clarify with the interviewer if rectangular.

---

### Q25. LRU Cache (Thread-Safe)
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Concurrent Programming / Data Structures

**Concept:**  
An LRU (Least Recently Used) cache evicts the least recently accessed item when full. `LinkedHashMap` maintains insertion + access order natively, making it the perfect backing data structure. Thread safety wraps it with `synchronized`.

**Approach:**
- Extend `LinkedHashMap` with `accessOrder = true`
- Override `removeEldestEntry` to evict when size exceeds capacity
- Wrap with `Collections.synchronizedMap` for thread safety

**Solution (Java 8):**
```java
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private final Map<K, V> cache;
    private final int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // true = access-order (most recently accessed at end), false = insertion-order
        this.cache = Collections.synchronizedMap(
            new LinkedHashMap<K, V>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > capacity;   // evict when over capacity
                }
            }
        );
    }

    public V get(K key) {
        return cache.getOrDefault(key, null);    // access updates order
    }

    public void put(K key, V value) {
        cache.put(key, value);                   // put updates order
    }

    public void display() {
        System.out.println(cache);
    }

    public static void main(String[] args) {
        LRUCache<Integer, String> lru = new LRUCache<>(3);

        lru.put(1, "Medicine A");
        lru.put(2, "Medicine B");
        lru.put(3, "Medicine C");
        lru.get(1);                              // access 1 → moves to most recent
        lru.put(4, "Medicine D");               // evicts least recent = 2

        lru.display();         // {3=Medicine C, 1=Medicine A, 4=Medicine D}
        System.out.println("Get key 2: " + lru.get(2));   // null (evicted)
        System.out.println("Get key 1: " + lru.get(1));   // Medicine A
    }
}
```

**Key Interview Points:**
- `LinkedHashMap(capacity, loadFactor, accessOrder=true)` — the third `true` is the key parameter.
- For a fully custom thread-safe solution, interviewers may ask for `ReentrantReadWriteLock` — mention it.

---

### Q26. Create a Deadlock
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐  
**Category:** Concurrent Programming

**Concept:**  
A deadlock occurs when two or more threads each hold a lock the other needs, causing permanent blocking. This is a demonstration question to show you understand thread synchronization, lock ordering, and the four Coffman conditions.

**Approach:**
- Thread 1 acquires `lockA`, then tries to acquire `lockB`
- Thread 2 acquires `lockB`, then tries to acquire `lockA`
- Both block waiting for the other — deadlock
- Prevention: always acquire locks in the same order across all threads

**Solution (Java 8):**
```java
public class DeadlockDemo {
    private static final Object lockA = new Object(); // Resource A
    private static final Object lockB = new Object(); // Resource B

    public static void main(String[] args) {

        // ── Thread 1: acquires A then tries B ─────────────────────
        Thread thread1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("Thread 1 acquired Lock A");
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                System.out.println("Thread 1 waiting for Lock B...");
                synchronized (lockB) {                   // BLOCKS — Thread 2 holds B
                    System.out.println("Thread 1 acquired Lock B");
                }
            }
        }, "Thread-1");

        // ── Thread 2: acquires B then tries A ─────────────────────
        Thread thread2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("Thread 2 acquired Lock B");
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                System.out.println("Thread 2 waiting for Lock A...");
                synchronized (lockA) {                   // BLOCKS — Thread 1 holds A
                    System.out.println("Thread 2 acquired Lock A");
                }
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Both will hang — detect via: jstack <pid>
    }
}

/*  ─── How to PREVENT deadlock ───────────────────────────────────
    1. Lock Ordering: always acquire locks in the same fixed order.
    2. tryLock with timeout: ReentrantLock.tryLock(timeout, TimeUnit)
    3. Avoid nested locks where possible.
    4. Use java.util.concurrent utilities (Semaphore, CountDownLatch).

    ─── Four Coffman conditions for deadlock ──────────────────────
    1. Mutual Exclusion  – only one thread can hold a resource
    2. Hold and Wait     – holding one lock while waiting for another
    3. No Preemption     – locks can't be taken away forcibly
    4. Circular Wait     – circle of threads each waiting on the next
*/
```

**Key Interview Points:**
- Run `jstack <pid>` on a running JVM to detect deadlock — interviewers ask how to diagnose it.
- Modern alternative: `ReentrantLock.tryLock(timeout, unit)` avoids indefinite blocking.

---

# EASY TIER — CONTINUED

---

### Q27. Sum of Digits
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** Mathematical Logic

**Concept:**  
Sum the individual digits of a number (e.g., 1234 → 1+2+3+4 = 10). The stream approach converts the number to a string, streams its characters, maps each back to its numeric value, and sums them.

**Approach:**
- `String.valueOf(n)` gives the digit characters
- `chars()` streams each character as an int (ASCII)
- Subtract `'0'` from each char to get the actual digit value
- `sum()` adds them all up

**Stream Solution (Java 8):**
```java
public class SumOfDigits {
    public static int sumStream(int n) {
        return String.valueOf(Math.abs(n))   // handle negatives
            .chars()                          // stream of char ASCII values
            .map(c -> c - '0')               // ASCII → digit (e.g. '5'=53 → 5)
            .sum();
    }

    // ── Traditional: modulo approach ─────────────────────────────
    public static int sumTraditional(int n) {
        n = Math.abs(n);
        int sum = 0;
        while (n > 0) { sum += n % 10; n /= 10; }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(sumStream(1234));       // 10
        System.out.println(sumStream(-9876));      // 30
        System.out.println(sumTraditional(9999));  // 36
    }
}
```

**Key Interview Points:**
- The `c - '0'` trick converts a char digit to its int value — a fundamental Java pattern.
- Always handle negative numbers with `Math.abs()` before processing digits.

---

### Q28. Even / Odd Partition
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Split a list into two groups: even numbers and odd numbers. `Collectors.partitioningBy` does exactly this — it returns a `Map<Boolean, List<T>>` where `true` = matches predicate, `false` = doesn't match. One of the most practical Stream collector questions.

**Approach:**
- `Collectors.partitioningBy(n -> n % 2 == 0)` splits on the even condition
- Result: `{true=[2,4,6], false=[1,3,5]}`
- Access with `map.get(true)` for evens, `map.get(false)` for odds

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvenOddPartition {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // ── partitioningBy splits into exactly 2 groups ───────────
        Map<Boolean, List<Integer>> partition = numbers.stream()
            .collect(Collectors.partitioningBy(n -> n % 2 == 0));

        System.out.println("Even: " + partition.get(true));   // [2, 4, 6, 8, 10]
        System.out.println("Odd:  " + partition.get(false));  // [1, 3, 5, 7, 9]

        // ── Bonus: count each group ───────────────────────────────
        Map<Boolean, Long> counts = numbers.stream()
            .collect(Collectors.partitioningBy(
                n -> n % 2 == 0,
                Collectors.counting()));                      // downstream collector
        System.out.println("Even count: " + counts.get(true));  // 5
        System.out.println("Odd count:  " + counts.get(false)); // 5
    }
}
```

**Key Interview Points:**
- `partitioningBy` is a special case of `groupingBy` with exactly two groups (true/false).
- Use it when you need a binary split; use `groupingBy` for 3+ categories.

---

### Q29. Reverse Words in a Sentence
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Reverse the order of words in a sentence: "Hello World Java" → "Java World Hello". Classic two-step stream operation: split into words, collect in reverse order.

**Approach:**
- Split sentence by spaces using `split("\\s+")`
- Convert to a `List`, then stream it
- Use `sorted` with reverse index, or push to a `Deque` and pop, or use `Collections.reverse`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReverseWords {
    public static String reverseWords(String sentence) {
        String[] words = sentence.trim().split("\\s+");     // split on any whitespace
        List<String> list = Arrays.asList(words);
        Collections.reverse(list);                          // in-place reverse
        return String.join(" ", list);
    }

    // ── Pure Stream variant using IntStream index trick ──────────
    public static String reverseWordsStream(String sentence) {
        String[] words = sentence.trim().split("\\s+");
        return java.util.stream.IntStream
            .rangeClosed(1, words.length)
            .mapToObj(i -> words[words.length - i])         // pick from end
            .collect(Collectors.joining(" "));
    }

    public static void main(String[] args) {
        System.out.println(reverseWords("Hello World Java"));          // Java World Hello
        System.out.println(reverseWordsStream("I love coding"));       // coding love I
        System.out.println(reverseWords("  spaces  around  "));        // around spaces
    }
}
```

**Key Interview Points:**
- `trim()` + `split("\\s+")` handles multiple spaces between words.
- Follow-up: "Reverse characters within each word too" — use the reverse-string method on each word via `stream().map()`.

---

### Q30. Convert List to Uppercase
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Transform every string in a list to uppercase. This is the simplest `map()` example — ideal for demonstrating method references with `String::toUpperCase`.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UppercaseList {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("alice", "bob", "carol", "dave");

        // ── map() with method reference ───────────────────────────
        List<String> upper = names.stream()
            .map(String::toUpperCase)                   // method reference
            .collect(Collectors.toList());

        System.out.println(upper);  // [ALICE, BOB, CAROL, DAVE]

        // ── Chain with filter: only names longer than 3 chars ────
        names.stream()
            .filter(n -> n.length() > 3)                // alice, carol, dave
            .map(String::toUpperCase)
            .forEach(System.out::println);              // ALICE CAROL DAVE
    }
}
```

**Key Interview Points:**
- `String::toUpperCase` is a method reference equivalent to `s -> s.toUpperCase()`.
- Know all four types: static (`Integer::parseInt`), instance (`String::toUpperCase`), arbitrary-instance (`s -> s.toUpperCase()`), constructor (`ArrayList::new`).

---

### Q31. Count Words in a String
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Count distinct words (or total words) in a sentence. Stream makes distinct word counting trivial with `distinct().count()`.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;

public class WordCount {
    public static void main(String[] args) {
        String sentence = "java is great and java is fun and easy";

        // ── Total word count ──────────────────────────────────────
        long total = Arrays.stream(sentence.split("\\s+"))
            .count();
        System.out.println("Total words: " + total);       // 9

        // ── Distinct word count ───────────────────────────────────
        long distinct = Arrays.stream(sentence.split("\\s+"))
            .distinct()
            .count();
        System.out.println("Distinct words: " + distinct); // 6

        // ── Longest word ──────────────────────────────────────────
        Arrays.stream(sentence.split("\\s+"))
            .max(java.util.Comparator.comparingInt(String::length))
            .ifPresent(w -> System.out.println("Longest: " + w)); // great or easy
    }
}
```

**Key Interview Points:**
- `split("\\s+")` splits on one or more whitespace characters — handles tabs and multiple spaces.
- `count()` is a terminal operation that returns `long`, not `int`.

---

### Q32. Armstrong Number Check
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐  
**Category:** Mathematical Logic

**Concept:**  
An Armstrong number (narcissistic number) equals the sum of its own digits each raised to the power of the number of digits. E.g., 153 = 1³ + 5³ + 3³ = 153. Stream makes the digit power-sum computation elegant.

**Stream Solution (Java 8):**
```java
public class ArmstrongNumber {
    public static boolean isArmstrong(int n) {
        String digits = String.valueOf(n);
        int power = digits.length();             // number of digits

        int sumOfPowers = digits.chars()
            .map(c -> c - '0')                   // char → digit
            .map(d -> (int) Math.pow(d, power))  // raise each digit to power
            .sum();

        return sumOfPowers == n;
    }

    public static void main(String[] args) {
        System.out.println(isArmstrong(153));    // true  (1³+5³+3³=153)
        System.out.println(isArmstrong(370));    // true  (3³+7³+0³=370)
        System.out.println(isArmstrong(9474));   // true  (9⁴+4⁴+7⁴+4⁴=9474)
        System.out.println(isArmstrong(100));    // false

        // ── Bonus: find all 3-digit Armstrong numbers ─────────────
        System.out.println("All 3-digit Armstrong numbers:");
        java.util.stream.IntStream.rangeClosed(100, 999)
            .filter(ArmstrongNumber::isArmstrong)
            .forEach(System.out::println);   // 153, 370, 371, 407
    }
}
```

**Key Interview Points:**
- `Math.pow(d, power)` returns `double` — cast back to `int` carefully.
- Finding all Armstrong numbers in a range with `IntStream.rangeClosed().filter()` is a great stream showcase.

---

### Q33. Collectors.joining — Build Sentences
**Difficulty:** Easy | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
`Collectors.joining` concatenates stream elements into a single string with optional delimiter, prefix, and suffix. Extremely useful for building CSV rows, SQL `IN` clauses, or formatted output.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JoiningDemo {
    public static void main(String[] args) {
        List<String> fruits = Arrays.asList("Apple", "Banana", "Cherry", "Date");

        // ── Simple join ───────────────────────────────────────────
        String simple = fruits.stream()
            .collect(Collectors.joining());
        System.out.println(simple);     // AppleBananaCherryDate

        // ── Join with delimiter ───────────────────────────────────
        String csv = fruits.stream()
            .collect(Collectors.joining(", "));
        System.out.println(csv);        // Apple, Banana, Cherry, Date

        // ── Join with delimiter, prefix, suffix ───────────────────
        String formatted = fruits.stream()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println(formatted);  // [Apple, Banana, Cherry, Date]

        // ── Practical: build SQL IN clause ────────────────────────
        List<Integer> ids = Arrays.asList(1, 2, 3, 4);
        String sql = ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", ", "WHERE id IN (", ")"));
        System.out.println(sql);        // WHERE id IN (1, 2, 3, 4)
    }
}
```

**Key Interview Points:**
- Three overloads: `joining()`, `joining(delimiter)`, `joining(delimiter, prefix, suffix)`.
- Equivalent to `String.join()` but composable inside a larger stream pipeline.

---

# MEDIUM TIER — CONTINUED

---

### Q34. FlatMap — Flatten List of Lists
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
`flatMap` transforms each element into a stream and then merges all those mini-streams into one flat stream. Essential for working with nested collections. One of the most-asked Java 8 Stream questions in mid-level interviews.

**Approach:**
- `stream().flatMap(Collection::stream)` flattens `List<List<T>>` into `Stream<T>`
- Think: `map` gives `Stream<Stream<T>>`, `flatMap` gives `Stream<T>`
- Also used to split strings: `flatMap(s -> Arrays.stream(s.split(" ")))`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlatMapDemo {
    public static void main(String[] args) {

        // ── Flatten List<List<Integer>> ───────────────────────────
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );

        List<Integer> flat = nested.stream()
            .flatMap(List::stream)                   // each inner list → stream
            .collect(Collectors.toList());

        System.out.println(flat);      // [1, 2, 3, 4, 5, 6, 7, 8, 9]

        // ── Flatten all words from list of sentences ──────────────
        List<String> sentences = Arrays.asList(
            "Hello World",
            "Java Streams are powerful",
            "FlatMap rocks"
        );

        List<String> words = sentences.stream()
            .flatMap(s -> Arrays.stream(s.split(" ")))  // split each sentence
            .map(String::toLowerCase)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        System.out.println(words);
        // [are, flatmap, hello, java, powerful, rocks, streams, world]

        // ── Unique characters across all strings ──────────────────
        long uniqueChars = sentences.stream()
            .flatMapToInt(String::chars)              // each string → IntStream of chars
            .distinct()
            .count();
        System.out.println("Unique chars: " + uniqueChars);
    }
}
```

**Key Interview Points:**
- `flatMap` vs `map`: `map` = one element → one element; `flatMap` = one element → zero or more elements.
- `flatMapToInt`, `flatMapToLong`, `flatMapToDouble` exist for primitive streams.

---

### Q35. Collectors.partitioningBy (Advanced)
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Use `partitioningBy` with a downstream collector to not just split into two groups but also perform aggregation within each group. E.g., partition employees into "above average salary" and "below average" groups, then count each.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PartitioningAdvanced {
    public static void main(String[] args) {
        // Using Employee from Q18
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 75000, "IT"),
            new Employee("Bob",   45000, "HR"),
            new Employee("Carol", 90000, "Finance"),
            new Employee("Dave",  55000, "IT"),
            new Employee("Eve",   40000, "HR")
        );

        // ── Partition: salary >= 60000 vs salary < 60000 ──────────
        Map<Boolean, List<Employee>> partitioned = employees.stream()
            .collect(Collectors.partitioningBy(e -> e.getSalary() >= 60000));

        System.out.println("High earners:");
        partitioned.get(true).forEach(System.out::println);

        System.out.println("\nLow earners:");
        partitioned.get(false).forEach(System.out::println);

        // ── Partition + count in each partition ───────────────────
        Map<Boolean, Long> counts = employees.stream()
            .collect(Collectors.partitioningBy(
                e -> e.getSalary() >= 60000,
                Collectors.counting()));
        System.out.println("\nCounts → " + counts);   // {false=2, true=3}

        // ── Partition + average salary in each group ──────────────
        Map<Boolean, Double> avgSalary = employees.stream()
            .collect(Collectors.partitioningBy(
                e -> e.getSalary() >= 60000,
                Collectors.averagingInt(Employee::getSalary)));
        System.out.println("Avg salary high: " + avgSalary.get(true));
        System.out.println("Avg salary low:  " + avgSalary.get(false));
    }
}
```

**Key Interview Points:**
- The two-arg form `partitioningBy(predicate, downstream)` is what interviewers are looking for.
- Common downstream collectors: `counting()`, `averaging*()`, `joining()`, `toList()`, `summarizingInt()`.

---

### Q36. Collectors.toMap
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Convert a list of objects into a `Map` using `Collectors.toMap`. The key is knowing how to handle key/value extractors and merge functions for duplicate keys.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ToMapDemo {
    public static void main(String[] args) {

        // ── List of Employee → Map<name, salary> ─────────────────
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 75000, "IT"),
            new Employee("Bob",   45000, "HR"),
            new Employee("Carol", 90000, "Finance")
        );

        Map<String, Integer> nameSalary = employees.stream()
            .collect(Collectors.toMap(
                Employee::getName,      // key extractor
                Employee::getSalary));  // value extractor

        System.out.println(nameSalary);
        // {Alice=75000, Bob=45000, Carol=90000}

        // ── Map<name, Employee> (value = whole object) ────────────
        Map<String, Employee> nameToEmp = employees.stream()
            .collect(Collectors.toMap(
                Employee::getName,
                Function.identity()));  // value = the employee object itself

        System.out.println(nameToEmp.get("Alice"));

        // ── Handle duplicate keys with merge function ─────────────
        List<String> words = Arrays.asList("apple", "ant", "bear", "banana", "cat");
        Map<Character, String> firstByLetter = words.stream()
            .collect(Collectors.toMap(
                w -> w.charAt(0),           // key = first letter
                Function.identity(),         // value = word
                (existing, replacement) -> existing  // on duplicate: keep first
            ));
        System.out.println(firstByLetter);
        // {a=apple, b=bear, c=cat}
    }
}
```

**Key Interview Points:**
- `toMap` throws `IllegalStateException` on duplicate keys without a merge function — always provide one for safety.
- Third argument (merge function) determines conflict resolution: keep first, keep last, concatenate, etc.

---

### Q37. Optional — Null Safety
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Java Core

**Concept:**  
`Optional<T>` is a container that may or may not hold a non-null value. It eliminates `NullPointerException` by making absence of a value explicit. Every Java 8 interview asks about `Optional`.

**Approach:**
- `Optional.of(value)` — value must not be null
- `Optional.ofNullable(value)` — value may be null
- `Optional.empty()` — explicitly empty
- Methods: `isPresent()`, `get()`, `orElse()`, `orElseGet()`, `orElseThrow()`, `map()`, `filter()`, `flatMap()`

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OptionalDemo {
    // Simulate a DB lookup that may return null
    static String findUserById(int id) {
        if (id == 1) return "Alice";
        if (id == 2) return "Bob";
        return null;   // not found
    }

    public static void main(String[] args) {

        // ── Basic usage ───────────────────────────────────────────
        Optional<String> user = Optional.ofNullable(findUserById(1));
        System.out.println(user.isPresent());              // true
        System.out.println(user.get());                    // Alice

        Optional<String> missing = Optional.ofNullable(findUserById(99));
        System.out.println(missing.orElse("Guest"));       // Guest
        System.out.println(missing.orElseGet(() -> "Anonymous")); // lazy evaluation

        // ── Chain with map() ──────────────────────────────────────
        Optional.ofNullable(findUserById(2))
            .map(String::toUpperCase)                      // BOB
            .filter(n -> n.length() > 2)                   // BOB passes
            .ifPresent(System.out::println);               // prints BOB

        // ── In a Stream pipeline ──────────────────────────────────
        List<Integer> ids = Arrays.asList(1, 99, 2, 100);
        ids.stream()
            .map(id -> Optional.ofNullable(findUserById(id)))
            .filter(Optional::isPresent)                   // skip empties
            .map(Optional::get)
            .forEach(System.out::println);                 // Alice, Bob

        // ── orElseThrow ───────────────────────────────────────────
        try {
            missing.orElseThrow(() -> new RuntimeException("User not found"));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());            // User not found
        }
    }
}
```

**Key Interview Points:**
- `orElse(default)` always evaluates `default`; `orElseGet(supplier)` is lazy — use `orElseGet` when default is expensive.
- Never call `optional.get()` without checking `isPresent()` — that defeats the purpose.

---

### Q38. String Compression
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** String Manipulation

**Concept:**  
Compress a string so that consecutive repeated characters are replaced with the character followed by its count: "aabbbcccc" → "a2b3c4". If no compression is achieved, return the original. This tests character grouping logic.

**Approach:**
- Track current character and its consecutive count using a loop
- When character changes, append `char + count` to the result
- Stream approach: `groupingBy` with a run-length key doesn't preserve consecutive groups — use traditional loop, show stream for verification

**Solution (Java 8):**
```java
public class StringCompression {
    // ── Traditional O(n) ─────────────────────────────────────────
    public static String compress(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        int count = 1;

        for (int i = 1; i <= s.length(); i++) {
            if (i < s.length() && s.charAt(i) == s.charAt(i - 1)) {
                count++;                               // same char, increment count
            } else {
                sb.append(s.charAt(i - 1));            // append the char
                if (count > 1) sb.append(count);       // append count if > 1
                count = 1;                             // reset counter
            }
        }

        // Return compressed only if shorter
        return sb.length() < s.length() ? sb.toString() : s;
    }

    // ── Stream: verify that compressed decompresses correctly ────
    public static String decompress(String compressed) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < compressed.length(); i++) {
            char c = compressed.charAt(i);
            if (Character.isLetter(c)) {
                if (i + 1 < compressed.length() && Character.isDigit(compressed.charAt(i + 1))) {
                    int count = Character.getNumericValue(compressed.charAt(i + 1));
                    java.util.stream.IntStream.range(0, count)
                        .forEach(x -> result.append(c));
                    i++;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(compress("aabbbcccc"));    // a2b3c4
        System.out.println(compress("abcd"));         // abcd (no compression)
        System.out.println(compress("aaabbaaa"));     // a3b2a3
        System.out.println(decompress("a2b3c4"));     // aabbbcccc
    }
}
```

**Key Interview Points:**
- Off-by-one errors are common — loop to `i <= s.length()` to catch the last group.
- `IntStream.range(0, count).forEach(x -> result.append(c))` is a good stream showcase for the repeat operation.

---

### Q39. Two Sum Problem
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Given an array and a target, find two indices whose values add up to the target. The brute-force is O(n²); the HashMap approach is O(n). One of the most universally asked coding interview questions.

**Approach:**
- One-pass HashMap: store each number's index as you iterate
- For each number `n`, check if `target - n` is already in the map
- If yes, return the pair; if no, add `n` to the map

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TwoSum {
    // ── O(n) HashMap approach ────────────────────────────────────
    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();   // value → index
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];          // what we need
            if (seen.containsKey(complement)) {
                return new int[]{seen.get(complement), i};  // found!
            }
            seen.put(nums[i], i);                       // store current
        }
        return new int[]{};                             // no solution
    }

    // ── Brute force O(n²) for comparison ──────────────────────────
    public static int[] twoSumBrute(int[] nums, int target) {
        return IntStream.range(0, nums.length)
            .boxed()
            .flatMap(i -> IntStream.range(i + 1, nums.length)
                .filter(j -> nums[i] + nums[j] == target)
                .mapToObj(j -> new int[]{i, j}))
            .findFirst()
            .orElse(new int[]{});
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));  // [0, 1]
        System.out.println(Arrays.toString(twoSum(new int[]{3, 2, 4}, 6)));       // [1, 2]
        System.out.println(Arrays.toString(twoSumBrute(new int[]{1, 5, 3, 7}, 8))); // [1, 3]
    }
}
```

**Key Interview Points:**
- HashMap solution is O(n) time and O(n) space — state both.
- This pattern (store complements while iterating) generalizes to Three Sum, Four Sum, etc.

---

### Q40. Comparable vs Comparator
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Java Core

**Concept:**  
`Comparable` defines the "natural ordering" of an object (one fixed sort order implemented inside the class). `Comparator` defines external, ad-hoc ordering (multiple sort strategies outside the class). Both are essential Java knowledge.

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// ── Comparable: natural order = salary ascending ──────────────────
class Product implements Comparable<Product> {
    String name;
    double price;
    int stock;

    Product(String name, double price, int stock) {
        this.name = name; this.price = price; this.stock = stock;
    }

    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price); // natural = price asc
    }

    @Override
    public String toString() { return name + " ₹" + price + " (stock:" + stock + ")"; }
}

public class ComparableVsComparator {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
            new Product("Paracetamol", 25.0, 200),
            new Product("Aspirin",     10.0, 500),
            new Product("Ibuprofen",   45.0, 100),
            new Product("Amoxicillin", 30.0, 150)
        );

        // ── Comparable: sort uses compareTo() ─────────────────────
        products.stream()
            .sorted()                               // uses Comparable.compareTo
            .forEach(System.out::println);
        // Aspirin, Paracetamol, Amoxicillin, Ibuprofen

        System.out.println();

        // ── Comparator: external, multiple strategies ─────────────
        Comparator<Product> byName = Comparator.comparing(p -> p.name);
        Comparator<Product> byStock = Comparator.comparingInt(p -> p.stock);
        Comparator<Product> byPriceDesc = Comparator.comparingDouble((Product p) -> p.price)
                                                     .reversed();

        // Sort by name
        products.stream().sorted(byName).forEach(System.out::println);

        System.out.println();
        // Sort by price descending then by name
        products.stream()
            .sorted(byPriceDesc.thenComparing(byName))
            .forEach(System.out::println);
    }
}
```

**Key Interview Points:**
- `Comparable` → `compareTo()` inside the class. `Comparator` → `compare()` outside.
- Use `Comparable` for one canonical sort; use `Comparator` for situational/multiple sorts.
- `Integer.compare(a,b)` / `Double.compare(a,b)` avoid the subtraction pitfall in `compareTo`.

---

### Q41. Sum & Average of Employee Salaries
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Aggregate numeric fields from a collection of objects using `mapToInt().sum()`, `.average()`, `.summaryStatistics()`. This is a direct test of numeric stream methods available via `mapToInt/mapToDouble/mapToLong`.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalaryAggregation {
    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Alice", 75000, "IT"),
            new Employee("Bob",   45000, "HR"),
            new Employee("Carol", 90000, "Finance"),
            new Employee("Dave",  55000, "IT"),
            new Employee("Eve",   40000, "HR")
        );

        // ── Total salary ──────────────────────────────────────────
        int total = employees.stream()
            .mapToInt(Employee::getSalary)
            .sum();
        System.out.println("Total:   ₹" + total);      // 305000

        // ── Average salary ────────────────────────────────────────
        double avg = employees.stream()
            .mapToInt(Employee::getSalary)
            .average()
            .orElse(0.0);
        System.out.println("Average: ₹" + avg);         // 61000.0

        // ── summaryStatistics: all aggregates in one pass ────────
        IntSummaryStatistics stats = employees.stream()
            .mapToInt(Employee::getSalary)
            .summaryStatistics();
        System.out.println("Count: " + stats.getCount());   // 5
        System.out.println("Max:   " + stats.getMax());     // 90000
        System.out.println("Min:   " + stats.getMin());     // 40000
        System.out.println("Sum:   " + stats.getSum());     // 305000
        System.out.println("Avg:   " + stats.getAverage()); // 61000.0

        // ── Average salary per department ─────────────────────────
        Map<String, Double> avgByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.averagingInt(Employee::getSalary)));
        System.out.println("Avg by dept: " + avgByDept);
    }
}
```

**Key Interview Points:**
- `summaryStatistics()` is a single-pass operation — more efficient than calling sum, average, max, min separately.
- `average()` returns `OptionalDouble` because an empty stream has no average — always handle it.

---

### Q42. Count Elements by Condition
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Count, check existence, or check universality of elements matching a condition using `count()`, `anyMatch()`, `allMatch()`, `noneMatch()`. These short-circuit terminal operations are fundamental stream knowledge.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;

public class CountAndMatch {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(2, 4, 6, 7, 8, 10, 11, 12);

        // ── count(): how many match a condition ───────────────────
        long evenCount = numbers.stream()
            .filter(n -> n % 2 == 0)
            .count();
        System.out.println("Even numbers: " + evenCount);     // 6

        // ── anyMatch(): is there at least one match? ──────────────
        boolean hasOdd = numbers.stream()
            .anyMatch(n -> n % 2 != 0);                        // short-circuits at 7
        System.out.println("Has odd: " + hasOdd);             // true

        // ── allMatch(): do ALL elements match? ────────────────────
        boolean allPositive = numbers.stream()
            .allMatch(n -> n > 0);
        System.out.println("All positive: " + allPositive);   // true

        // ── noneMatch(): does NONE of them match? ─────────────────
        boolean noneNegative = numbers.stream()
            .noneMatch(n -> n < 0);
        System.out.println("None negative: " + noneNegative); // true

        // ── Practical: validate a list of email addresses ─────────
        List<String> emails = Arrays.asList("a@b.com", "user@test.org", "bad-email");
        boolean allValid = emails.stream()
            .allMatch(e -> e.contains("@") && e.contains("."));
        System.out.println("All emails valid: " + allValid);  // false
    }
}
```

**Key Interview Points:**
- `anyMatch`, `allMatch`, `noneMatch` short-circuit — they stop processing as soon as the answer is determined.
- `count()` does NOT short-circuit — it must process all elements.

---

### Q43. Merge Two Sorted Arrays
**Difficulty:** Medium | **Frequency:** ⭐⭐⭐⭐  
**Category:** Arrays

**Concept:**  
Merge two sorted arrays into one sorted array. The classic two-pointer approach is O(m+n) without extra sorting cost. `Stream.concat` provides a clean stream alternative but requires a final sort.

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MergeSortedArrays {
    // ── Stream approach: concat + sort ───────────────────────────
    public static int[] mergeStream(int[] a, int[] b) {
        return IntStream.concat(                         // concat two IntStreams
            Arrays.stream(a),
            Arrays.stream(b))
            .sorted()                                    // sort the result
            .toArray();
    }

    // ── Traditional O(m+n) two-pointer (optimal) ─────────────────
    public static int[] mergeOptimal(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) result[k++] = a[i++];     // pick smaller
            else               result[k++] = b[j++];
        }
        while (i < a.length) result[k++] = a[i++];      // remaining from a
        while (j < b.length) result[k++] = b[j++];      // remaining from b
        return result;
    }

    public static void main(String[] args) {
        int[] a = {1, 3, 5, 7};
        int[] b = {2, 4, 6, 8};

        System.out.println(Arrays.toString(mergeStream(a, b)));   // [1,2,3,4,5,6,7,8]
        System.out.println(Arrays.toString(mergeOptimal(a, b)));  // [1,2,3,4,5,6,7,8]

        int[] x = {1, 4, 7, 9};
        int[] y = {2, 3, 5};
        System.out.println(Arrays.toString(mergeOptimal(x, y)));  // [1,2,3,4,5,7,9]
    }
}
```

**Key Interview Points:**
- Stream approach is O((m+n) log(m+n)) due to sort; two-pointer is O(m+n) — explain the trade-off.
- `IntStream.concat` vs `Stream.concat` — use `IntStream.concat` for primitive arrays.

---

# HARD TIER — CONTINUED

---

### Q44. CompletableFuture — Async Tasks
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Concurrent Programming

**Concept:**  
`CompletableFuture` (Java 8) enables asynchronous, non-blocking computation with composable callbacks. It replaces the old `Future` + `ExecutorService.submit` pattern. Essential for senior-level interviews.

**Approach:**
- `supplyAsync(supplier)` — runs a task asynchronously, returns a result
- `thenApply(fn)` — transforms the result (like `map`)
- `thenAccept(consumer)` — consumes the result (like `forEach`)
- `thenCombine(other, fn)` — combines two futures
- `exceptionally(fn)` — fallback on exception

**Solution (Java 8):**
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CompletableFutureDemo {
    // Simulate slow DB call
    static String fetchUser(int id) throws Exception {
        Thread.sleep(500);
        return "User_" + id;
    }

    // Simulate slow pricing service
    static double fetchPrice(String item) throws Exception {
        Thread.sleep(300);
        return item.length() * 10.5;
    }

    public static void main(String[] args) throws Exception {

        // ── Basic async task ──────────────────────────────────────
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {                         // runs on ForkJoinPool
                try { return fetchUser(1); }
                catch (Exception e) { throw new RuntimeException(e); }
            })
            .thenApply(String::toUpperCase)              // transform result
            .thenApply(s -> "Hello, " + s);              // chain another transform

        System.out.println(future.get());                // Hello, USER_1

        // ── Run two tasks in parallel & combine ───────────────────
        CompletableFuture<String> userFuture = CompletableFuture
            .supplyAsync(() -> "Alice");

        CompletableFuture<String> greetFuture = CompletableFuture
            .supplyAsync(() -> "Good Morning");

        CompletableFuture<String> combined = userFuture
            .thenCombine(greetFuture,                    // combine two futures
                (user, greet) -> greet + ", " + user + "!");

        System.out.println(combined.get());              // Good Morning, Alice!

        // ── Exception handling ────────────────────────────────────
        CompletableFuture<String> safe = CompletableFuture
            .supplyAsync(() -> { throw new RuntimeException("DB error"); })
            .exceptionally(ex -> "Fallback: " + ex.getMessage());

        System.out.println(safe.get());                  // Fallback: java.lang.RuntimeException: DB error

        // ── Run all N tasks, wait for all ─────────────────────────
        CompletableFuture<Void> all = CompletableFuture.allOf(
            CompletableFuture.runAsync(() -> System.out.println("Task 1")),
            CompletableFuture.runAsync(() -> System.out.println("Task 2")),
            CompletableFuture.runAsync(() -> System.out.println("Task 3"))
        );
        all.get();                                       // waits for all 3
    }
}
```

**Key Interview Points:**
- `thenApply` is synchronous continuation; `thenApplyAsync` uses a separate thread.
- `allOf` waits for ALL futures; `anyOf` returns when the FIRST one completes.

---

### Q45. ExecutorService / Thread Pool
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Concurrent Programming

**Concept:**  
`ExecutorService` manages a pool of threads to execute tasks without the overhead of creating a new thread per task. Interviewers expect knowledge of pool types, `submit` vs `execute`, and proper shutdown.

**Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExecutorServiceDemo {
    public static void main(String[] args) throws Exception {

        // ── Fixed thread pool: at most 3 threads active at once ───
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Submit 5 tasks using Stream
        List<Future<String>> futures = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> pool.submit(() -> {           // Callable returns a value
                Thread.sleep(200);
                return "Task " + i + " done by " + Thread.currentThread().getName();
            }))
            .collect(Collectors.toList());

        // Collect results
        for (Future<String> f : futures) {
            System.out.println(f.get());                 // blocks until result ready
        }

        // ── ALWAYS shutdown to prevent resource leak ───────────────
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // ── Cached thread pool: creates threads on demand ─────────
        ExecutorService cached = Executors.newCachedThreadPool();
        cached.execute(() -> System.out.println("Fire and forget task"));
        cached.shutdown();

        // ── ScheduledExecutorService: delayed / periodic tasks ────
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            () -> System.out.println("Heartbeat: " + System.currentTimeMillis()),
            0, 1, TimeUnit.SECONDS);              // initial delay=0, period=1s
        Thread.sleep(3000);
        scheduler.shutdown();
    }
}
```

**Key Interview Points:**
- `execute(Runnable)` — fire and forget; `submit(Callable)` — returns a `Future` for the result.
- Never forget `shutdown()` + `awaitTermination()` — thread pools are not GC'd automatically.

---

### Q46. Producer-Consumer Using BlockingQueue
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Concurrent Programming

**Concept:**  
The Producer-Consumer pattern decouples data production from consumption. `BlockingQueue` handles thread synchronization automatically: `put()` blocks when full, `take()` blocks when empty. Eliminates the need for manual `wait()/notify()`.

**Solution (Java 8):**
```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumer {
    static final int CAPACITY = 5;
    static final String POISON_PILL = "DONE"; // signal to stop consumer

    static class Producer implements Runnable {
        private final BlockingQueue<String> queue;
        Producer(BlockingQueue<String> q) { this.queue = q; }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 10; i++) {
                    String item = "Medicine_" + i;
                    queue.put(item);                     // blocks if queue is full
                    System.out.println("Produced: " + item + " | Queue size: " + queue.size());
                    Thread.sleep(100);
                }
                queue.put(POISON_PILL);                  // signal end of production
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final BlockingQueue<String> queue;
        Consumer(BlockingQueue<String> q) { this.queue = q; }

        @Override
        public void run() {
            try {
                while (true) {
                    String item = queue.take();          // blocks if queue is empty
                    if (POISON_PILL.equals(item)) break; // stop signal received
                    System.out.println("Consumed: " + item);
                    Thread.sleep(200);                   // consumer is slower
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(CAPACITY);

        Thread producer = new Thread(new Producer(queue), "Producer");
        Thread consumer = new Thread(new Consumer(queue), "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println("All done.");
    }
}
```

**Key Interview Points:**
- `ArrayBlockingQueue` is bounded; `LinkedBlockingQueue` can be unbounded (risky — can OOM).
- The "poison pill" (sentinel value) is the cleanest way to signal end-of-stream to consumers.

---

### Q47. Top-N Frequent Words
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
Find the top N most frequently occurring words in a text. Combines `groupingBy + counting`, `sorted by value descending`, and `limit(N)`. A practical test of stream chaining depth.

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TopNWords {
    public static Map<String, Long> topN(String text, int n) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))  // split on non-word chars
            .filter(w -> !w.isEmpty())                           // remove empty tokens
            .collect(Collectors.groupingBy(
                Function.identity(),                             // group by word
                Collectors.counting()))                          // count each word
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // most frequent first
            .limit(n)                                            // take top N
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new));                            // preserve rank order
    }

    public static void main(String[] args) {
        String text = "java is great java is fun java streams are great streams are powerful";

        Map<String, Long> top3 = topN(text, 3);
        System.out.println("Top 3 words: " + top3);
        // {java=3, streams=2, great=2}

        // ── Bonus: top 5 from a paragraph ─────────────────────────
        String para = "the quick brown fox jumps over the lazy dog the fox the";
        topN(para, 5).forEach((word, count) ->
            System.out.printf("  %-10s → %d%n", word, count));
    }
}
```

**Key Interview Points:**
- The `LinkedHashMap::new` in `toMap`'s 4th argument preserves insertion (rank) order — without it, order is not guaranteed.
- The `\\W+` regex splits on any non-word character — handles commas, periods, and other punctuation automatically.

---

### Q48. Valid Parentheses
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐⭐  
**Category:** Stack / String

**Concept:**  
Check if a string of brackets `()`, `[]`, `{}` is valid — every opening bracket must be closed in the correct order. Classic stack problem. Streams help reduce the check but the core logic needs a stack.

**Approach:**
- Push every opening bracket onto a stack
- When a closing bracket is encountered, pop and verify it matches
- At the end, the stack must be empty

**Solution (Java 8):**
```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ValidParentheses {
    public static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();

        // Map each closing bracket to its matching opening bracket (Java 8)
        Map<Character, Character> pairs = new HashMap<>();
        pairs.put(')', '(');
        pairs.put(']', '[');
        pairs.put('}', '{');

        for (char c : s.toCharArray()) {
            if ("([{".indexOf(c) >= 0) {
                stack.push(c);                           // opening → push onto stack
            } else if (pairs.containsKey(c)) {           // closing bracket
                if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                    return false;                        // mismatch or unmatched
                }
            }
        }
        return stack.isEmpty();                          // all brackets matched
    }

    // ── Stream version: validate character by character ──────────
    // Note: Stream can't replace the stack logic (stateful scan needed)
    // but we can stream over input for processing
    public static void main(String[] args) {
        System.out.println(isValid("()[]{}"));     // true
        System.out.println(isValid("([{}])"));     // true
        System.out.println(isValid("(]"));         // false
        System.out.println(isValid("{[}]"));       // false
        System.out.println(isValid(""));           // true (empty is valid)

        // Validate multiple expressions using Stream
        java.util.stream.Stream.of("()", "()[]{}", "(]", "([)]", "{[]}")
            .forEach(expr -> System.out.println(expr + " → " + isValid(expr)));
    }
}
```

**Key Interview Points:**
- Use `Deque<Character>` (backed by `ArrayDeque`) not `Stack` — `Stack` is legacy and has synchronization overhead.
- `Map.of` (Java 9+) syntax shown; use `new HashMap<>()` + `.put()` for Java 8 compatibility.

---

### Q49. Immutable Class Design
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Java Core

**Concept:**  
An immutable class cannot be modified after creation (like `String`, `Integer`). Safe to share across threads without synchronization. Five rules: `final` class, `private final` fields, no setters, deep copy in constructor, return copies of mutable fields.

**Solution (Java 8):**
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ── Rule 1: declare the class final ───────────────────────────────
public final class ImmutableMedicine {

    // ── Rule 2: all fields private final ──────────────────────────
    private final String name;
    private final double price;
    private final List<String> ingredients;  // mutable field!

    // ── Rule 3: set via constructor only ──────────────────────────
    public ImmutableMedicine(String name, double price, List<String> ingredients) {
        this.name = name;
        this.price = price;
        // ── Rule 4: deep copy mutable parameters in constructor ───
        this.ingredients = new ArrayList<>(ingredients); // defensive copy
    }

    // ── Rule 5: getters return copies of mutable fields ───────────
    public String getName()    { return name; }
    public double getPrice()   { return price; }

    public List<String> getIngredients() {
        return Collections.unmodifiableList(ingredients); // read-only view
    }

    // No setters!

    @Override
    public String toString() {
        return "ImmutableMedicine{name='" + name + "', price=" + price
               + ", ingredients=" + ingredients + "}";
    }

    public static void main(String[] args) {
        List<String> ings = new ArrayList<>();
        ings.add("Compound A");
        ings.add("Compound B");

        ImmutableMedicine med = new ImmutableMedicine("Paracetamol", 25.0, ings);
        System.out.println(med);

        // Mutate the original list → does NOT affect immutable object
        ings.add("Compound C");
        System.out.println("After mutation: " + med.getIngredients()); // still 2 ingredients

        // Try to modify returned list → UnsupportedOperationException
        try {
            med.getIngredients().add("Compound D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify: " + e.getClass().getSimpleName());
        }
    }
}
```

**Key Interview Points:**
- The #1 mistake: forgetting defensive copies for mutable fields (`List`, `Date`, `arrays`).
- Java's `String` is immutable which is why `String` is safe as a `HashMap` key.

---

### Q50. Stream reduce() — Custom Aggregation
**Difficulty:** Hard | **Frequency:** ⭐⭐⭐⭐  
**Category:** Stream API

**Concept:**  
`reduce()` is the most powerful stream terminal operation — it combines all elements into a single result using an accumulator function. Behind the scenes, `sum()`, `max()`, `count()` are all built on `reduce`. Understanding it deeply sets you apart in interviews.

**Approach:**
- `reduce(identity, accumulator)` — starts with identity, applies accumulator cumulatively
- `reduce(accumulator)` — returns `Optional<T>` (no identity for empty stream)
- `reduce(identity, accumulator, combiner)` — three-arg form for parallel streams

**Stream Solution (Java 8):**
```java
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReduceDemo {
    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // ── Sum using reduce (reimplementing .sum()) ──────────────
        int sum = numbers.stream()
            .reduce(0, Integer::sum);           // 0 is identity; sum(a,b) = a+b
        System.out.println("Sum: " + sum);      // 15

        // ── Product of all numbers ────────────────────────────────
        int product = numbers.stream()
            .reduce(1, (a, b) -> a * b);        // identity=1 for multiplication
        System.out.println("Product: " + product); // 120

        // ── Max using reduce (reimplementing .max()) ──────────────
        Optional<Integer> max = numbers.stream()
            .reduce(Integer::max);              // no identity → Optional
        System.out.println("Max: " + max.orElse(0)); // 5

        // ── Concatenate strings with reduce ──────────────────────
        List<String> words = Arrays.asList("Java", "Stream", "Reduce");
        String sentence = words.stream()
            .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
        System.out.println(sentence);           // Java Stream Reduce

        // ── Factorial using reduce ────────────────────────────────
        int n = 6;
        long factorial = java.util.stream.LongStream
            .rangeClosed(1, n)
            .reduce(1L, Long::multiplyExact);
        System.out.println(n + "! = " + factorial); // 6! = 720

        // ── Custom object aggregation: build total order ──────────
        // Each int[] = {quantity, unitPrice}
        int totalRevenue = Arrays.asList(
            new int[]{2, 100},   // 2 units × ₹100
            new int[]{3, 200},   // 3 units × ₹200
            new int[]{1, 50})    // 1 unit  × ₹50
            .stream()
            .reduce(0,
                (acc, order) -> acc + order[0] * order[1],  // accumulate qty*price
                Integer::sum);                               // combiner (for parallel)
        System.out.println("Total Revenue: ₹" + totalRevenue); // ₹850
    }
}
```

**Key Interview Points:**
- Identity value must be neutral for the operation: `0` for sum, `1` for product, `""` for concatenation.
- The three-arg `reduce(identity, accumulator, combiner)` is required when the accumulator type differs from the stream element type.
- `reduce` creates a new value each step; for mutable accumulation (building a `List`), use `collect` instead.

---

## Summary Cheat Sheet

| # | Question | One-Line Key Insight | Frequency |
|---|----------|----------------------|-----------|
| 1 | Swap Numbers | XOR avoids overflow | ⭐⭐⭐ |
| 2 | Reverse String | `IntStream.rangeClosed` + pick from end | ⭐⭐⭐⭐⭐ |
| 3 | Palindrome | `IntStream.range(len/2).allMatch(mirror)` | ⭐⭐⭐⭐⭐ |
| 4 | Vowel Count | `chars().filter("aeiou".indexOf ≥ 0)` | ⭐⭐⭐⭐ |
| 5 | Factorial | `LongStream.rangeClosed(1,n).reduce(multiplyExact)` | ⭐⭐⭐⭐ |
| 6 | Prime Check | `IntStream(2,√n).noneMatch(n%i==0)` | ⭐⭐⭐⭐ |
| 7 | Custom Exception | Extend RuntimeException, add context fields | ⭐⭐⭐ |
| 8 | Max/Min | `stream().max(naturalOrder())` returns `Optional` | ⭐⭐⭐ |
| 9 | Fibonacci | `Stream.iterate({0,1}, f→{f[1],f[0]+f[1]}).limit(n)` | ⭐⭐⭐⭐⭐ |
| 10 | Anagram | `chars().sorted()` compare both strings | ⭐⭐⭐⭐ |
| 11 | Missing Number | Expected sum − actual sum (IntStream) | ⭐⭐⭐⭐⭐ |
| 12 | First Non-Repeated | `groupingBy + LinkedHashMap` (order matters!) | ⭐⭐⭐⭐⭐ |
| 13 | Second Largest | `distinct().sorted(reversed()).skip(1).findFirst()` | ⭐⭐⭐⭐ |
| 14 | Remove Duplicates | `stream().distinct()` or two-pointer | ⭐⭐⭐⭐ |
| 15 | Filter starts with '1' | `filter(n → valueOf(n).startsWith("1"))` | ⭐⭐⭐⭐⭐ |
| 16 | Find Duplicates | `groupingBy + counting` or `!Set.add()` trick | ⭐⭐⭐⭐⭐ |
| 17 | Grouping & Counting | `groupingBy(identity(), LinkedHashMap::new, counting())` | ⭐⭐⭐⭐ |
| 18 | Sort Employees | `comparingInt().reversed().thenComparing()` | ⭐⭐⭐⭐ |
| 19 | Binary Search | `mid = low + (high-low)/2` avoids overflow | ⭐⭐⭐⭐ |
| 20 | Singleton | Enum = safest; DCL needs `volatile` | ⭐⭐⭐⭐ |
| 21 | Rotate Array | Reverse all → reverse k → reverse rest | ⭐⭐⭐ |
| 22 | Kadane's | `currentMax = max(arr[i], currentMax+arr[i])` | ⭐⭐⭐⭐ |
| 23 | Longest Substring | HashMap jump = O(n); HashSet two-pointer = O(n) | ⭐⭐⭐⭐⭐ |
| 24 | Matrix Rotation | Transpose + reverse rows = 90° clockwise | ⭐⭐⭐ |
| 25 | LRU Cache | `LinkedHashMap(cap, 0.75f, true)` + removeEldest | ⭐⭐⭐⭐ |
| 26 | Deadlock | Opposite lock order → fix with consistent ordering | ⭐⭐⭐ |
| 27 | Sum of Digits | `chars().map(c → c-'0').sum()` | ⭐⭐⭐⭐ |
| 28 | Even/Odd Partition | `partitioningBy(n → n%2==0)` → Map<Boolean,List> | ⭐⭐⭐⭐⭐ |
| 29 | Reverse Words | `IntStream.rangeClosed(1,len).mapToObj(pick from end)` | ⭐⭐⭐⭐ |
| 30 | List to Uppercase | `stream().map(String::toUpperCase)` method reference | ⭐⭐⭐ |
| 31 | Count Words | `Arrays.stream(split).distinct().count()` | ⭐⭐⭐ |
| 32 | Armstrong Number | `chars().map(d^power).sum() == n` + `IntStream.rangeClosed` | ⭐⭐⭐ |
| 33 | Collectors.joining | `joining(", ", "[", "]")` — delimiter+prefix+suffix | ⭐⭐⭐⭐ |
| 34 | FlatMap | `flatMap(List::stream)` turns Stream<List<T>> → Stream<T> | ⭐⭐⭐⭐⭐ |
| 35 | partitioningBy (adv) | `partitioningBy(pred, counting())` — downstream collector | ⭐⭐⭐⭐ |
| 36 | Collectors.toMap | `toMap(key, value, mergeFunction)` — merge handles duplicates | ⭐⭐⭐⭐ |
| 37 | Optional | `ofNullable().map().filter().orElseGet(supplier)` | ⭐⭐⭐⭐⭐ |
| 38 | String Compression | Loop with `char + count`; stream for repeat | ⭐⭐⭐⭐ |
| 39 | Two Sum | HashMap: store complement, check before insert, O(n) | ⭐⭐⭐⭐⭐ |
| 40 | Comparable vs Comparator | Comparable=inside class; Comparator=external strategy | ⭐⭐⭐⭐ |
| 41 | Salary Aggregation | `mapToInt().summaryStatistics()` — all aggregates in one pass | ⭐⭐⭐⭐ |
| 42 | Count/Match | `anyMatch` / `allMatch` / `noneMatch` — short-circuit | ⭐⭐⭐ |
| 43 | Merge Sorted Arrays | `IntStream.concat()+sorted()` or O(m+n) two-pointer | ⭐⭐⭐⭐ |
| 44 | CompletableFuture | `supplyAsync.thenApply.thenCombine.exceptionally` | ⭐⭐⭐⭐ |
| 45 | ExecutorService | `newFixedThreadPool(n)` + `submit(Callable)` + `shutdown()` | ⭐⭐⭐⭐ |
| 46 | Producer-Consumer | `BlockingQueue.put()/take()` + poison-pill stop signal | ⭐⭐⭐⭐ |
| 47 | Top-N Words | `groupingBy+counting → sorted by value → limit(N)` | ⭐⭐⭐⭐ |
| 48 | Valid Parentheses | Stack push opening; pop+match on closing; empty = valid | ⭐⭐⭐⭐⭐ |
| 49 | Immutable Class | `final` class + `final` fields + defensive copy in ctor | ⭐⭐⭐⭐ |
| 50 | Stream reduce() | `reduce(identity, accumulator)` — foundation of all aggregations | ⭐⭐⭐⭐ |

---

*Good luck with your coding round! Focus on ⭐⭐⭐⭐⭐ questions first — they appear in almost every interview.*
