# Настройка базы данных

1. Создать пользователя

```postgresql
create role <username> with login password '<password>';
```

2. Создать схему

```postgresql
create schema <schema> authorization <username>;
```

3. В приложении создать профиль соединения с параметрами, указанными в предыдущих пунктах

