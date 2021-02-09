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
    System.out.println(1);
    System.out.println(2);
}
```

After:
```java
Arrays.<Runnable>asList(
    () -> System.out.println(1),
    () -> System.out.println(2)
).forEach(Runnable::run)
```

## If statement

See [blocks](#block)

Before:
```java
if (condition) {
    System.out.println(1);
    System.out.println(2);
} else {
    System.out.println(3);
    System.out.println(4);
}
```

After:
```java
(condition ?
        Arrays.<Runnable>asList(
            () -> System.out.println(1),
            () -> System.out.println(2)
        )
        :
        Arrays.<Runnable>asList(
            () -> System.out.println(3),
            () -> System.out.println(4)
        )).forEach(Runnable::run)
```

## While statement

See [blocks](#block)

Before:
```java
while(condition) {
    System.out.println(1);
}
```

After:
```java
while(condition ?
        Arrays.<Runnable>asList(() -> System.out.println(1)).stream().peek(Runnable::run).allMatch(Objects::nonNull) 
        :
        false) {} 
```

### Break and continue
Break and continue are implemented using exceptions. For `break`, the while statement is wrapped and for `continue` the
body is wrapped. Each exceptions has a custom message id which we use to differentiate between break, continue and other
exceptions.

Before:
```java
int i = 0;
while(i < 5) {
    i++;
    if(i < 3)
        continue;
    if(i >= 3)
        break;
    i++;
}
                        
System.out.println(i);
```

After:
```java
Arrays.<Runnable>asList(
        () -> local.put(1,0), //intialize i
        () -> {
            try {{ //try-catch for break
                if(true ?
                    Arrays.<Runnable>asList(() -> {
                        while(((int)local.get(1))<5?Arrays.<Runnable>asList(() -> {
                            try {{ //try-catch for continue
                                if(true ? //if statement which runs the while body
                                    Arrays.<Runnable>asList(
                                        () -> local.put(1,((int)local.get(1))+1), //i++
                                        () -> (((int)local.get(1))<3 //if(i < 3)
                                                ?
                                                Arrays.<Runnable>asList(
                                                        () -> jdk.internal.misc.Unsafe.getUnsafe().throwException(new RuntimeException("jtjThrow2")) //continue
                                                )
                                                :
                                                Arrays.<Runnable>asList()).forEach(Runnable::run),
                                        () -> (((int)local.get(1))>=3 //if(i >= 3)
                                                ?
                                                Arrays.<Runnable>asList(
                                                    () -> jdk.internal.misc.Unsafe.getUnsafe().throwException(new RuntimeException("jtjThrow3")) //break
                                                )
                                                :
                                                Arrays.<Runnable>asList()).forEach(Runnable::run),
                                        () -> local.put(1,((int)local.get(1))+1) //i++
                                    ).stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {}
                            }} catch(Throwable jtjEx4) {{ //catch block for continue exceptions
                                if(true ? //if statement which runs the body of the try-catch that catches continues
                                    Arrays.<Runnable>asList(
                                        () -> (jtjEx4.getMessage().equals("jtjThrow2") //check for continue exception id
                                                ?
                                                Arrays.<Runnable>asList()
                                                :
                                                Arrays.<Runnable>asList(
                                                    () -> jdk.internal.misc.Unsafe.getUnsafe().throwException(jtjEx4) //re-throw otherwise
                                                )).forEach(Runnable::run)
                                    ).stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {} //end of if which checks for continue exceptions
                        }}
                    }).stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {} //end of while statement
                }).stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {} //end of if which holds the while
            }} catch(Throwable jtjEx5) {{ //catch block for break exceptions
                if(true ? //if statement which runs the body of the try-catch that catches breaks
                    Arrays.<Runnable>asList(
                        () -> (jtjEx5.getMessage().equals("jtjThrow3") //check for break exception id
                                ?
                                Arrays.<Runnable>asList()
                                :
                                Arrays.<Runnable>asList(
                                    () -> jdk.internal.misc.Unsafe.getUnsafe().throwException(jtjEx5) //re-throw otherwise
                                )).forEach(Runnable::run)
                    ).stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {} //end of if which checks for break exceptions
            }}
        },
        () -> System.out.println(((int)local.get(1))) //print i at the end
).forEach(Runnable::run)  
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
