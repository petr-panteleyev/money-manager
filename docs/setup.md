# Setup Database

1. Create user

```postgresql
create role <username> with login password '<password>';
```

2. Create schema

```postgresql
create schema <schema> authorization <username>;
```

3. Create connection profile using schema and user setup

