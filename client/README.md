# Client Library

**Пример**:

```java
var client = new MoneyClient.Builder()
        .withServerUrl("http://localhost:1705")
        .withConnectTimeout(Duration.ofMillis(1000))
        .build();
    
client.getAccounts().onRight(list -> {
        System.out.println(list);
});
```
