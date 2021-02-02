# Basic Syntax

## Statement

Before:
```java
System.out.println("Hi");
```

After:
```java
((Runnable) (() ->                                   
    System.out.println("Hi")                                  
)
```

## Block

See [statements](#statement)

Before:
```java
{
    /* statement1 */
    /* statement2 */
}
```

After:
```java
Arrays.asList(
    ((Runnable) (() ->
        /* statment1 */
    ), ((Runnable) (() ->
        /* statement2 */
    )
).forEach(Runnable::run)
```

## Main method

Before:
```java
public class Test {                         
    public static void main(String[] args) {
        System.out.println("Hi");           
    }                                       
}                                           
```

After:
```java
import java.util.function.BiFunction;                                     
public class Test {                                                       
    public static void main(String[] args) {                              
        if(((Function<HashMap<Integer, Object>, Boolean>) ((map) ->       
            Steam.of(((Runnable) (() ->                                   
                System.out.println("Hi")                                  
            )).peek(Runnable::run).findFirst().get() != null)             
        ).apply(new HashMap<>()))) {}                                     
    }                                                                     
}                                                                         
```

## If statement

See [blocks](#block)

Before:
```java
if (condition) {
    /* block1 */
} else {
    /* block2 */    
}
```

After:
```java
(condition ?
        /* block1 */ 
        :
        /* block2 */).forEach(Runnable::run)
```
