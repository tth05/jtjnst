# Basic Syntax

## Statement

Before:
```java
System.out.println("Hi");
```

After:
```java
() -> System.out.println("Hi")
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
Arrays.<Runnable>asList(
    () -> /* statement1 */,
    () -> /* statement2 */
).forEach(Runnable::run)
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

## Bare-bones program

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
public class Test {                                                       
    public static void main(String[] __args) {
        if(((Function<HashMap<Integer, Object>, Boolean>)((global)->Stream.<Runnable>of(() ->
            Arrays.<Runnable>asList(
                ()-> /*Static init*/,
                /*Add all methods*/
                ()-> /*Main method*/
                    global.put(0, (BiConsumer<List<Object>, List<Object>>)(
                        (args, retPtr)-> /*Arguments and return pointer*/
                            ((Consumer<HashMap<Integer, Object>>)(local-> /*Add local scope*/
                                Arrays.asList(
                                    () -> System.out.println("Hi")
                                ).forEach(Runnable::run)
                            )).accept(new HashMap<>())
                        )
                    ),
                /*Call program entry point / method 0 / main method*/
                ()-> ((BiConsumer<List<Object>, List<Object>>)global.get(0)).accept(Arrays.asList(__args), new ArrayList<>())
            ).forEach(Runnable::run)
        ).peek(Runnable::run).findFirst() == null)).apply(new HashMap<>())) {}                                 
    }                                                                     
}                                                                         
```

### Methods

Before:
```java
public class Test {                         
    public static void main(String[] args) {
        System.out.println("Main start");
        int i = 5;
        System.out.println("Return value is: " + foo(i));
    }          
    
    public static double foo(int i) { return i + 0.5d; }
}                                           
```

After:
```java
public class Test {                                                       
    public static void main(String[] __args) {
        if(((Function<HashMap<Integer, Object>, Boolean>)((global)->Stream.<Runnable>of(() ->
            Arrays.<Runnable>asList(
                ()-> /*Static init*/,
                /*Add all methods*/
                ()-> /*Main method*/
                    global.put(0, (BiConsumer<List<Object>, List<Object>>)(
                        (args, retPtr) -> /*Arguments and return pointer*/
                            ((Consumer<HashMap<Integer, Object>>)(local-> /*Add local scope*/
                                Arrays.<Runnable>asList(
                                    () -> System.out.println("Main start"), 
                                    () -> local.put(0, (Integer)5) /*Create object in local space*/,
                                    () -> local.put(1, Arrays.asList(new Object[1])) /* allocate return value space */,
                                    () -> ((BiConsumer<List<Object>, List<Object>>)global.get(1)).accept(Arrays.asList(local.get(0)), (List<Object>)local.get(1)) /* call foo */,
                                    () -> System.out.println("Return value is: " + ((List<Object>)local.get(1)).get(0))
                                ).forEach(Runnable::run)
                            )).accept(new HashMap<>())
                        )
                    ),
                ()-> /* double foo(int) */
                    global.put(1, (BiConsumer<List<Object>, List<Object>>)(
                        (args, retPtr) -> /*Arguments and return pointer*/
                            ((Consumer<HashMap<Integer, Object>>)(local->
                                Arrays.<Runnable>asList(
                                    () -> retPtr.set(0, ((Integer)args.get(0)) + 0.5)
                                ).forEach(Runnable::run)
                            )).accept(new HashMap<>())
                        )
                    ),
                /*Call program entry point / method 0 / main method*/
                ()-> ((BiConsumer<List<Object>, List<Object>>)global.get(0)).accept(Arrays.asList(__args), new ArrayList<>())
            ).forEach(Runnable::run)
        ).peek(Runnable::run).findFirst() == null)).apply(new HashMap<>())) {}                                 
    }                                                                     
}                                                                         
```
